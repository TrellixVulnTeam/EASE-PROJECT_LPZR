package com.Ease.Servlet.BackOffice;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.Ease.Context.Catalog.Catalog;
import com.Ease.Context.Catalog.Website;
import com.Ease.Dashboard.User.User;
import com.Ease.Utils.GeneralException;
import com.Ease.Utils.ServletManager;

/**
 * Servlet implementation class GetWebsitesDone
 */
@WebServlet("/GetWebsitesDone")
public class GetWebsitesDone extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetWebsitesDone() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher rd = request.getRequestDispatcher("admin.jsp");
		rd.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletManager sm = new ServletManager(this.getClass().getName(), request, response, true);
		User user = sm.getUser();
		try {
			sm.needToBeConnected();
			if (!user.isAdmin())
				throw new GeneralException(ServletManager.Code.ClientWarning, "You ain't admin");
			Catalog catalog = (Catalog) sm.getContextAttr("catalog");
			List<Website> websitesDone = catalog.getWorkingWebsites();
			JSONArray res = new JSONArray();
			for (Website websiteDone : websitesDone) {
				JSONObject tmp = new JSONObject();
				tmp.put("url", websiteDone.getHostname());
				tmp.put("id", websiteDone.getDb_id());
				tmp.put("count", websiteDone.getVisits());
				res.add(tmp);
			}
			sm.setResponse(ServletManager.Code.Success, res.toString());
			sm.setLogResponse("GetWebsitesDone success");
		} catch(GeneralException e) {
			sm.setResponse(e);
		}
		sm.sendResponse();
	}

}
