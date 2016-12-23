package com.Ease.Servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.Ease.Dashboard.User.User;
import com.Ease.Utils.DataBaseConnection;
import com.Ease.Utils.GeneralException;
import com.Ease.Utils.ServletManager;

/**
 * Servlet implementation class WebsiteRequest
 */
@WebServlet("/WebsiteRequest")
public class WebsiteRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WebsiteRequest() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User)(session.getAttribute("User"));
		ServletManager sm = new ServletManager(this.getClass().getName(), request, response, true);
		DataBaseConnection db = sm.getDB();
		try {
			sm.needToBeConnected();
			if (!user.isAdmin())
				throw new GeneralException(ServletManager.Code.ClientError, "You are not admin");
			ResultSet rs = db.get("SELECT * FROM requestedWebsites;");
			String retMsg = "";
			try {
				while(rs.next()) {
					ResultSet rs2 = db.get("SELECT firstName FROM users WHERE id=" + rs.getString(rs.findColumn("user_id")) +";");
					rs2.next();
					String name = rs2.getString(1); 
					retMsg += rs.getString(rs.findColumn("site")) + "-SENTBY-" + name + "-DATE-"+ rs.getString(rs.findColumn("date"));
					if (rs.next())
						retMsg += ";";
				}
					
			} catch (SQLException e) {
				throw new GeneralException(ServletManager.Code.InternError, e);
			}
			sm.setResponse(ServletManager.Code.Success, retMsg);
		} catch (GeneralException e) {
			sm.setResponse(e);
		} catch (Exception e) {
			sm.setResponse(e);
		}
		sm.sendResponse();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) (session.getAttribute("user"));
		ServletManager sm = new ServletManager(this.getClass().getName(), request, response, true);
		DataBaseConnection db = sm.getDB();
		try {
			sm.needToBeConnected();
			String site = sm.getServletParam("ask", true);
			db.set("INSERT INTO requestedWebsites values (null, " + user.getDBid() + ", '" + site + "', default");
		} catch (GeneralException e) {
			sm.setResponse(e);
		} catch (Exception e) {
			sm.setResponse(e);
		}
		sm.sendResponse();
	}

}
