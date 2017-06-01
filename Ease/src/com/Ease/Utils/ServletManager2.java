package com.Ease.Utils;

import com.Ease.Dashboard.User.User;
import com.Ease.Hibernate.HibernateQuery;
import com.Ease.Team.Team;
import com.Ease.Team.TeamManager;
import com.Ease.Team.TeamUser;
import com.Ease.websocket.WebsocketMessage;
import com.Ease.websocket.WebsocketSession;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class ServletManager2 {

    public enum HttpStatus {
        Success(200),
        BadRequest(400),
        AccessDenied(401),
        PaymentRequired(402),
        Forbidden(403),
        InternError(500);

        private final int status;

        HttpStatus(int status) {
            this.status = status;
        }

        public int getValue() {
            return status;
        }
    }

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected String redirectUrl;
    protected String servletName;
    protected Map<String, String> args;
    protected User user;
    protected TeamUser teamUser;
    protected DataBaseConnection db;
    protected boolean saveLogs;
    protected String logResponse;
    protected String socketId;
    protected List<WebsocketMessage> messages;
    protected JSONObject params;
    protected JSONObject jsonObjectResponse;
    protected JSONArray jsonArrayResponse;
    protected String errorMessage;

    protected HibernateQuery hibernateQuery = new HibernateQuery();

    public Map<String, WebsocketSession> websockets = new HashMap<String, WebsocketSession>();
    public static boolean debug = true;

    public ServletManager2(String servletName, HttpServletRequest request, HttpServletResponse response, boolean saveLogs) throws IOException {
        this.args = new HashMap<>();
        this.servletName = servletName;
        this.request = request;
        this.response = response;
        this.user = (User) request.getSession().getAttribute("user");
        this.teamUser = (TeamUser) request.getSession().getAttribute("teamUser");
        this.logResponse = null;
        this.redirectUrl = null;
        this.saveLogs = saveLogs;
        this.messages = new LinkedList<WebsocketMessage>();
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String json = "";
        String buffer;
        while ((buffer = br.readLine()) != null)
            json += buffer;
        br.close();
        JSONParser jsonParser = new JSONParser();
        try {
            params = (JSONObject) jsonParser.parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IOException();
        }
        try {
            this.db = new DataBaseConnection(DataBase.getConnection());
        } catch (SQLException e) {
            /* Do nothing */
        }
    }

    private void setSuccess() {
        response.setStatus(HttpStatus.Success.getValue());
        response.setContentType("application/json");
    }

    private void setInternError() {
        response.setStatus(HttpStatus.InternError.getValue());
    }

    private void setForbidden() {
        response.setStatus(HttpStatus.Forbidden.getValue());
    }

    private void setAccessDenied() {
        response.setStatus(HttpStatus.AccessDenied.getValue());
    }

    private void setBadRequest() {
        response.setStatus(HttpStatus.BadRequest.getValue());
    }

    public void setSuccess(String msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", msg);
        this.setSuccess(jsonObject);
    }

    public void setSuccess(JSONObject res) {
        this.setSuccess();
        this.jsonObjectResponse = res;
    }

    public void setSuccess(JSONArray res) {
        this.setSuccess();
        this.jsonArrayResponse = res;
    }

    public void setError(Exception e) {
        e.printStackTrace();
        try {
            HttpServletException httpServletException = (HttpServletException) e;
            System.out.println("Error code: " + httpServletException.getHttpStatus() + " and msg: " + httpServletException.getMsg());
            response.setStatus(httpServletException.getHttpStatus().getValue());
            this.errorMessage = httpServletException.getMsg();
        } catch (ClassCastException e1) {
            this.setInternError();
        }
    }

    public void needToBeConnected() throws HttpServletException {
        if (user == null) {
            throw new HttpServletException(HttpStatus.AccessDenied);
        } else {
            /*socketId = request.getParameter("socketId");
            if (!debug && socketId == null) {
				throw new GeneralException(Code.ClientError, "No socketId.");
			} else if (user.getWebsockets().containsKey(socketId) == false) {
				System.out.println(user.getWebsockets().size());
				throw new GeneralException(Code.ClientError, "Wrong socketId.");
			}*/
        }
    }

    public void needToBeTeamUser() throws HttpServletException {
        this.needToBeConnected();
        if (this.getUser().getTeamUsers().isEmpty())
            throw new HttpServletException(HttpStatus.Forbidden);
    }

    public void needToBeTeamUserOfTeam(String team_id) throws HttpServletException {
        this.needToBeTeamUser();
        for (TeamUser teamUser : this.getUser().getTeamUsers()) {
            if (teamUser.getTeam().getDb_id() == Integer.parseInt(team_id))
                return;
        }
        throw new HttpServletException(HttpStatus.Forbidden);
    }

    public void needToBeAdminOfTeam(Team team) throws HttpServletException {
        this.needToBeTeamUser();
        for (TeamUser teamUser : this.getUser().getTeamUsers()) {
            if (teamUser.getTeam() == team && teamUser.isTeamAdmin())
                return;
        }
        throw new HttpServletException(HttpStatus.Forbidden);
    }

    public void needToBeAdminOfTeam(Integer team_id) throws HttpServletException {
        this.needToBeTeamUser();
        for (TeamUser teamUser : this.getUser().getTeamUsers()) {
            if (teamUser.getTeam().getDb_id() == team_id && teamUser.isTeamAdmin())
                return;
        }
        throw new HttpServletException(HttpStatus.Forbidden);
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public Object getParam(String paramName, boolean saveInLogs) {
        Object param = params.get(paramName);
        if (saveInLogs)
            args.put(paramName, param.toString());
        return param;
    }

    public DataBaseConnection getDB() {
        return this.db;
    }

    public HibernateQuery getHibernateQuery() {
        return this.hibernateQuery;
    }

    public void setRedirectUrl(String url) {
        this.redirectUrl = url;
    }

    public void setLogResponse(String msg) {
        this.logResponse = msg;
    }

    private void saveLogs() throws GeneralException {
        String argsString = "";
        Set<Entry<String, String>> setHm = args.entrySet();
        Iterator<Entry<String, String>> it = setHm.iterator();
        while (it.hasNext()) {
            Entry<String, String> e = it.next();
            argsString += "<" + e.getKey() + ":" + e.getValue() + ">";
        }
        if (this.logResponse == null)
            this.logResponse = "Success";
        try {
            this.logResponse = URLEncoder.encode(this.logResponse, "UTF-8");
            argsString = URLEncoder.encode(argsString, "UTF-8");
            DatabaseRequest request = db.prepareRequest("INSERT INTO logs values(?, ?, ?, ?, ?, default);");
            request.setString(this.servletName);
            request.setInt(this.response.getStatus());
            if (this.user == null)
                request.setNull();
            else
                request.setInt(this.user.getDBid());
            request.setString(argsString);
            request.setString(this.logResponse);
            request.set();
        } catch (UnsupportedEncodingException e) {
            this.setInternError();
        }

    }

    public void sendResponse() {
        user = (request.getSession().getAttribute("user") == null) ? user : (User) request.getSession().getAttribute("user");

        if (this.saveLogs) {
            try {
                saveLogs();
            } catch (GeneralException e) {
                System.out.println(e.getMsg());
                System.err.println("Logs not sended to database.");
            }
        }

        if (this.response.getStatus() != HttpStatus.Success.getValue()) {
            try {
                this.db.rollbackTransaction();
                this.hibernateQuery.rollback();
            } catch (GeneralException e) {
                System.err.println("Rollback transaction failed.");
            }
        }

        try {
            //System.out.println("wMessages loop start");
            /*for (WebsocketMessage msg : this.messages) {
                websockets.forEach((key, socket) -> {
					System.out.println( (user == null ? "No user" : user.getFirstName()) + " client socketId : " + key + ", sm socketId : " + socketId);
					if (msg.getWho() == WebsocketMessage.Who.ALLTABS ||
							(msg.getWho() == WebsocketMessage.Who.OTHERTABS && (! key.equals(socketId))) ||
							(msg.getWho() == WebsocketMessage.Who.THISTAB && key.equals(socketId))) {
							try {
								System.out.println("Send message to " + key);
								socket.sendMessage(msg);
								System.out.println("Message sent to " + key);
							} catch (IOException e) {
								websockets.remove(key, socket);
							}
						}
				});
			}*/
            //System.out.println("wMessages loop done");
            if (this.redirectUrl != null) {
                System.out.println("redirect to " + this.redirectUrl);
                response.sendRedirect(this.redirectUrl);
            } else {
                if (this.jsonArrayResponse == null && this.jsonObjectResponse == null) {
                    this.setInternError();
                    if (this.errorMessage != null)
                        response.getWriter().print(this.errorMessage);
                    else
                        response.sendError(response.getStatus());
                } else {
                    if (this.jsonArrayResponse != null)
                        response.getWriter().print(this.jsonArrayResponse.toString());
                    else
                        response.getWriter().print(this.jsonObjectResponse.toString());
                    this.hibernateQuery.commit();
                }
            }
        } catch (IOException e) {
            System.err.println("Send response failed.");
        }
        db.close();
    }

    public Object getContextAttr(String attr) {
        return request.getServletContext().getAttribute(attr);
    }

    public ServletContext getServletContext() {
        return request.getServletContext();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        this.getSession().setAttribute("user", user);
    }

    public TeamUser getTeamUser() {
        return teamUser;
    }

    public void setSocketId(String socketId) {
        if (debug && socketId == null)
            return;
        if (socketId == null)
            return;
        this.socketId = socketId;
    }

    public void addWebsockets(Map<String, WebsocketSession> websocketsMap) {
        if (debug && websocketsMap == null)
            return;
        if (websocketsMap == null)
            return;
        websockets.putAll(websocketsMap);
    }

    public void addWebsocket(String tabId, WebsocketSession websocket) throws GeneralException {
        if (debug && (websocket == null || tabId == null))
            return;
        if (websocket == null || tabId == null)
            return;
        websockets.put(tabId, websocket);
    }

    public void removeWebsocket(String tabId) {
        if (debug && tabId == null)
            return;
        if (tabId == null)
            return;
        websockets.remove(tabId);
    }

    public void addToSocket(WebsocketMessage msg) {
        if (debug && msg == null)
            return;
        if (msg == null)
            return;
        messages.add(msg);
    }

    public HttpSession getSession() {
        return request.getSession();
    }

    /**
     * Return the next unique single_id for a context object
     *
     * @return int
     */
    public int getNextSingle_id() {
        return ((IdGenerator) this.getContextAttr("idGenerator")).getNextId();
    }

    public List<TeamUser> getTeamUsers() {
        return this.getUser().getTeamUsers();
    }

    public TeamUser getTeamUserForTeam(Team team) throws HttpServletException {
        for (TeamUser teamUser : this.getTeamUsers()) {
            if (teamUser.getTeam() == team)
                return teamUser;
        }
        throw new HttpServletException(HttpStatus.BadRequest);
    }

    public TeamUser getTeamUserForTeamId(Integer team_id) throws HttpServletException {
        try {
            TeamManager teamManager = (TeamManager) this.getContextAttr("teamManager");
            Team team = null;
            team = teamManager.getTeamWithId(team_id);
            for (TeamUser teamUser : this.getTeamUsers()) {
                if (teamUser.getTeam() == team)
                    return teamUser;
            }
        } catch (GeneralException e) {
            throw new HttpServletException(HttpStatus.BadRequest);
        }
        return null;
    }
}
