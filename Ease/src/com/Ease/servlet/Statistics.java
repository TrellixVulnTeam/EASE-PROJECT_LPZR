package com.Ease.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.temporal.ChronoUnit;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.Ease.context.DataBase;
import com.Ease.data.ServletItem;
import com.Ease.session.User;

/**
 * Servlet implementation class Statistics
 */
@WebServlet("/Statistics")
public class Statistics extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Statistics() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		RequestDispatcher rd = request.getRequestDispatcher("admin.jsp");
		rd.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("User");
		ServletItem SI = new ServletItem(ServletItem.Type.AdminStats, request, response, user);
		if (!user.isAdmin(session.getServletContext())) {
			response.getWriter().print("error: You aint admin bro");
			return;
		}

		/* Connect db */
		DataBase db = (DataBase) session.getServletContext().getAttribute("DataBase");
		db.connect();
		String dbRequest = null;

		String startDate = null;
		startDate = SI.getServletParam("startDate");
		String endDate = null;
		endDate = SI.getServletParam("endDate");
		String baseRequest = "SELECT count(distinct users.user_id) from logs join users on logs.user_id = users.user_id WHERE";
		JSONObject jsonRes = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date sDate = null;
		Date eDate = null;
		try {
			sDate = sdf.parse(startDate);
			eDate = sdf.parse(endDate);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		LocalDate start = sDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate end = eDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1);

		String dailyConnections = SI.getServletParam("dailyConnections");
		String registeredUsers = SI.getServletParam("registeredUsers");
		String registeredUsersWithTuto = SI.getServletParam("registeredUsersWithTuto");
		String appsAddedParam = SI.getServletParam("appsAdded");
		String appsRemovedParam = SI.getServletParam("appsRemoved");

		JSONArray dates = new JSONArray();

		for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1))
			dates.add(date.toString());

		if (dailyConnections != null)
			jsonRes.put("dailyConnections", getDailyConnections(db, start, end, SI));

		if (registeredUsers != null)
			jsonRes.put("registeredUsers", getRegisteredUsers(db, start, end, SI));

		if (registeredUsersWithTuto != null)
			jsonRes.put("registeredUsersWithTuto", getRegisteredUsersWithTuto(db, start, end, SI));

		if (appsAddedParam != null)
			jsonRes.put("appsAdded", getAppsAdded(db, start, end, SI));

		if (appsRemovedParam != null)
			jsonRes.put("appsRemoved", getAppsRemoved(db, start, end, SI));

		if (!jsonRes.isEmpty())
			jsonRes.put("dates", dates);
		SI.setResponse(200, jsonRes.toString());
		SI.sendResponse();
	}

	public JSONObject getJsonObjectFor(JSONArray values, String label, String backgroundColor, String borderColor,
			String chart) {
		JSONObject resObj = new JSONObject();
		resObj.put("values", values);
		resObj.put("label", label);
		resObj.put("backgroundColor", backgroundColor);
		resObj.put("borderColor", borderColor);
		resObj.put("chart", chart);
		return resObj;
	}

	public JSONObject getDailyConnections(DataBase db, LocalDate startDate, LocalDate endDate, ServletItem SI) {
		JSONArray values = new JSONArray();
		ResultSet rs = db.get(newRequest(startDate, endDate,
				" AND tuto = 1 AND code = 200 AND type = " + ServletItem.Type.ConnectionServlet.ordinal()));
		try {
			while (rs.next())
				values.add(Integer.parseInt(rs.getString(2)));
		} catch (SQLException e) {
			SI.setResponse(ServletItem.Code.LogicError, e.getStackTrace().toString());
			e.printStackTrace();
		}
		return getJsonObjectFor(values, "Daily connections", "rgba(132, 99, 255, 0.2)", "rgba(132, 99, 255, 1)",
				"usersChart");
	}

	public JSONObject getRegisteredUsers(DataBase db, LocalDate startDate, LocalDate endDate, ServletItem SI) {
		JSONArray values = new JSONArray();
		ResultSet rs = db.get(newRequest(startDate, endDate,
				" AND code = 200 AND type = " + ServletItem.Type.RegistrationByInvitation.ordinal()));
		try {
			while (rs.next())
				values.add(Integer.parseInt(rs.getString(2)));
		} catch (SQLException e) {
			SI.setResponse(ServletItem.Code.LogicError, e.getStackTrace().toString());
			e.printStackTrace();
		}
		return getJsonObjectFor(values, "Registred users", "rgba(132, 255, 99, 0.2)", "rgba(132, 255, 99, 1)",
				"usersChart");
	}

	public JSONObject getRegisteredUsersWithTuto(DataBase db, LocalDate startDate, LocalDate endDate, ServletItem SI) {
		JSONArray values = new JSONArray();
		ResultSet rs = db.get(newRequest(startDate, endDate,
				" AND code = 200 AND tuto = 1 AND type = " + ServletItem.Type.RegistrationByInvitation.ordinal()));
		try {
			while (rs.next())
				values.add(Integer.parseInt(rs.getString(2)));
		} catch (SQLException e) {
			SI.setResponse(ServletItem.Code.LogicError, e.getStackTrace().toString());
			e.printStackTrace();
		}
		return getJsonObjectFor(values, "Registred users with tuto", "rgba(255, 132, 99, 0.2)", "rgba(255, 132, 99, 1)",
				"usersChart");
	}

	public String getDailyUsersRequest(LocalDate startDate, LocalDate endDate) {
		return "(SELECT user_id FROM (SELECT DISTINCT user_id, CAST(date AS DATE) AS date FROM logs where code = 200 AND type = 9 AND CAST(date AS DATE)  between '"
				+ startDate.toString() + "' and '" + endDate.toString()
				+ "') AS tmp GROUP BY user_id HAVING count(date) = " + ChronoUnit.DAYS.between(startDate, endDate)
				+ ") as DailyUsers";
	}

	public JSONObject getAverageConnectionsPerDailyUsers(DataBase db, LocalDate startDate, LocalDate endDate,
			String dailyUsersRequest, ServletItem SI) {
		ResultSet rs = db
				.get("SELECT date, count(*) from (SELECT CAST(date AS DATE) AS date FROM logs JOIN " + dailyUsersRequest
						+ " ON DailyUsers.user_id = logs.user_id where type = 7 AND code = 200) as tmp group BY date;");
		ResultSet rs2 = db.get("SELECT count(*) FROM " + dailyUsersRequest + ";");
		int nbCo = -1;
		int dailyUsers = -1;
		JSONArray jArray = new JSONArray();
		int res = -1;
		try {
			while (rs2.next())
				dailyUsers = Integer.parseInt(rs2.getString(1));
			while (rs.next()) {
				System.out.println(rs.getString(1));
				jArray.add(Integer.parseInt(rs.getString(2)));
			}

		} catch (SQLException e) {
			SI.setResponse(ServletItem.Code.LogicError, e.getStackTrace().toString());
			e.printStackTrace();
		}
		return getJsonObjectFor(jArray, "Average site connection per day per user", "rgba(255, 99, 132, 0.2)",
				"rgba(255, 99, 132, 1)", "connectionsChart");
	}

	public String newRequest(LocalDate startDate, LocalDate endDate, String conditions) {
		return ("SELECT d.selected_date, count(logs.user_id) FROM (SELECT selected_date from (SELECT adddate('1970-01-01',t4.i*10000 + t3.i*1000 + t2.i*100 + t1.i*10 + t0.i)  selected_date FROM (select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t0,   (select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t1,  (select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t2,  (select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t3,  (select 0 i union select 1 union select 2 union select 3 union select 4 union select 5 union select 6 union select 7 union select 8 union select 9) t4) v   where selected_date between '"
				+ startDate.toString() + "' AND '" + endDate.toString()
				+ "' GROUP BY selected_date ) d LEFT OUTER JOIN (logs JOIN users ON logs.user_id = users.user_id) ON d.selected_date = CAST(logs.date AS DATE)"
				+ conditions + " GROUP BY d.selected_date ORDER BY 1 ASC;");
	}

	public JSONObject getAppsAdded(DataBase db, LocalDate startDate, LocalDate endDate, ServletItem SI) {
		JSONArray values = new JSONArray();
		ResultSet rs = db.get(newRequest(startDate, endDate,
				" AND (type = " + ServletItem.Type.AddApp.ordinal() + " OR type = "
						+ ServletItem.Type.AddAppSso.ordinal() + " OR type = " + ServletItem.Type.AddLogWith.ordinal()
						+ ")"));
		try {
			while (rs.next()) {
				values.add(Integer.parseInt(rs.getString(2)));
			}
		} catch (SQLException e) {
			SI.setResponse(ServletItem.Code.LogicError, e.getStackTrace().toString());
			e.printStackTrace();
		}
		return getJsonObjectFor(values, "Apps added", "rgba(99, 132, 255, 0.2)", "rgba(99, 132, 255, 1)", "appsChart");
	}

	public JSONObject getAppsRemoved(DataBase db, LocalDate startDate, LocalDate endDate, ServletItem SI) {
		JSONArray values = new JSONArray();
		ResultSet rs = db.get(newRequest(startDate, endDate, " AND type = " + ServletItem.Type.DeleteApp.ordinal()));
		try {
			while (rs.next()) {
				values.add(Integer.parseInt(rs.getString(2)));
			}
		} catch (SQLException e) {
			SI.setResponse(ServletItem.Code.LogicError, e.getStackTrace().toString());
			e.printStackTrace();
		}
		return getJsonObjectFor(values, "Apps removed", "rgba(255, 99, 132, 0.2)", "rgba(255, 99, 132, 1)",
				"appsChart");
	}
}
