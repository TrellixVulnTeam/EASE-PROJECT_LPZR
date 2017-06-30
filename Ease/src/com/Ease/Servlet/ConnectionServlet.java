package com.Ease.Servlet;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.Ease.Dashboard.App.App;
import com.Ease.Dashboard.App.SharedApp;
import com.Ease.Dashboard.App.WebsiteApp.ClassicApp.ClassicApp;
import com.Ease.Dashboard.User.User;
import com.Ease.Hibernate.HibernateQuery;
import com.Ease.Team.TeamUser;
import com.Ease.Utils.*;
import com.Ease.Utils.Crypto.RSA;
import com.Ease.websocket.WebsocketSession;

/**
 * Servlet implementation class ConnectionServlet
 */
@WebServlet("/connection")
public class ConnectionServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int max_attempts = 10;
    private static final long expiration_time = 5; // 5 minutes
    private static final long ONE_MINUTE_IN_MILLIS = 60000;

    public ConnectionServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher("index.jsp");
        rd.forward(request, response);
    }

    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        ServletManager sm = new ServletManager(this.getClass().getName(), request, response, true);
        DataBaseConnection db = sm.getDB();
        // Get Parameters
        String email = sm.getServletParam("email", true);
        String password = sm.getServletParam("password", false);
        // --
        Map<String, WebsocketSession> sessionWebsockets = (Map<String, WebsocketSession>) session.getAttribute("sessionWebsockets");
        String client_ip = getIpAddr(request);
        User user = null;
        // Put current ip in db
        try {
            if (sm.getUser() != null)
                throw new HttpServletException(HttpStatus.BadRequest, "You are already logged");
            addIpInDataBase(client_ip, db);
            if (canConnect(client_ip, db)) {
                if (email == null || Regex.isEmail(email) == false)
                    sm.setResponse(ServletManager.Code.ClientWarning, "Wrong email or password.");
                else if (password == null || password.isEmpty())
                    sm.setResponse(ServletManager.Code.ClientWarning, "Wrong email or password.");
                else {
                    user = User.loadUser(email, password, sm);
                    sm.setUser(user);
                    HibernateQuery hibernateQuery = new HibernateQuery();
                    for (TeamUser teamUser : user.getTeamUsers()) {
                        if (!teamUser.isVerified() && teamUser.getTeamKey() != null) {
                            teamUser.finalizeRegistration();
                            hibernateQuery.saveOrUpdateObject(teamUser);
                        }
                        if (teamUser.isVerified() && teamUser.getTeamKey() != null && teamUser.isDisabled()) {
                            String deciphered_teamKey = RSA.Decrypt(teamUser.getTeamKey(), user.getKeys().getPrivateKey());
                            teamUser.setTeamKey(user.encrypt(deciphered_teamKey));
                            teamUser.setDeciphered_teamKey(deciphered_teamKey);
                            teamUser.setDisabled(false);
                            hibernateQuery.saveOrUpdateObject(teamUser);
                            for (SharedApp sharedApp : teamUser.getSharedApps()) {
                                sharedApp.setDisableShared(false, sm.getDB());
                            }
                        }
                    }
                    hibernateQuery.commit();
                    user.getDashboardManager().decipherApps(sm);
                    removeIpFromDataBase(client_ip, db);
                    sm.setResponse(ServletManager.Code.Success, "Successfully connected.");
                    //sm.addWebsockets(sessionWebsockets);
                    //sm.addToSocket(WebsocketMessage.connectionMessage());
                }
            } else {
                throw new GeneralException(ServletManager.Code.UserMiss, "Too much attempts to connect. Please retry in 5 minutes.");
            }
        } catch (GeneralException e) {
            sm.setResponse(e);
        } catch (Exception e) {
            sm.setResponse(e);
        }
        System.out.println("Send connection response");
        sm.sendResponse();
        System.out.println("Connection done");
    }

    public String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (null != ip && !"".equals(ip.trim()) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("X-Forwarded-For");
        if (null != ip && !"".equals(ip.trim()) && !"unknown".equalsIgnoreCase(ip)) {
            // get first ip from proxy ip
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    public void addIpInDataBase(String client_ip, DataBaseConnection db) throws GeneralException {
        DatabaseRequest request = db.prepareRequest("SELECT * FROM askingIps WHERE ip= ?;");
        request.setString(client_ip);
        DatabaseResult rs = request.get();
        if (rs.next())
            return;
        int transaction = db.startTransaction();
        request = db.prepareRequest("INSERT INTO askingIps values (NULL, ?, 0, ?, ?);");
        request.setString(client_ip);
        request.setString(getCurrentTime());
        request.setString(getExpirationTime());
        request.set();
        db.commitTransaction(transaction);
    }

    public String getCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public String getExpirationTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(new Date(date.getTime() + (expiration_time * ONE_MINUTE_IN_MILLIS)));
    }

    public void removeIpFromDataBase(String client_ip, DataBaseConnection db) throws GeneralException {
        DatabaseRequest request = db.prepareRequest("DELETE FROM askingIps WHERE ip = ?;");
        request.setString(client_ip);
        request.set();
    }

    public int incrementAttempts(String client_ip, DataBaseConnection db) throws GeneralException {
        DatabaseRequest request = db.prepareRequest("UPDATE askingIps SET attempts = attempts + 1, attemptDate = ?, expirationDate = ? WHERE ip = ?;");
        request.setString(getCurrentTime());
        request.setString(getExpirationTime());
        request.setString(client_ip);
        request.set();
        request = db.prepareRequest("select attempts from askingIps where ip= ?;");
        request.setString(client_ip);
        DatabaseResult rs = request.get();
        rs.next();
        try {
            return Integer.parseInt(rs.getString(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean canConnect(String client_ip, DataBaseConnection db) throws GeneralException {
        DatabaseRequest request = db.prepareRequest("SELECT attempts, expirationDate FROM askingIps WHERE ip= ?;");
        request.setString(client_ip);
        DatabaseResult rs = request.get();
        int attempts = 0;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date expirationDate = new Date();
        try {
            if (rs.next()) {
                attempts = Integer.parseInt(rs.getString(1));
                expirationDate = dateFormat.parse(rs.getString(2));
            }
        } catch (Exception e) {
            throw new GeneralException(ServletManager.Code.InternError, e);
        }
        return attempts < max_attempts || expirationDate.compareTo(new Date()) <= 0;
    }
}