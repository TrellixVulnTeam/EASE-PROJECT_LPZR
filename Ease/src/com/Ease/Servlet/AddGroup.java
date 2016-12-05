package com.Ease.Servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.Ease.Context.Group.Group;
import com.Ease.Context.Group.GroupProfile;
import com.Ease.Context.Group.Infrastructure;
import com.Ease.Utils.GeneralException;
import com.Ease.Utils.ServletManager;

/**
 * Servlet implementation class AddGroup
 */
@WebServlet("/AddGroup")
public class AddGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddGroup() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher rd = request.getRequestDispatcher("index.jsp");
		rd.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletManager sm = new ServletManager(this.getClass().getName(), request, response, true);
		
		try {
			String name = sm.getServletParam("name", true);
			String parent_id = sm.getServletParam("parent_id", true);
			Group parent = null;
			Infrastructure infra = null;
			sm.needToBeConnected();
			Map<Integer, Group> groups = (Map<Integer, Group>) sm.getContextAttr("groups");
			if (name == null || name.equals(""))
				throw new GeneralException(ServletManager.Code.ClientWarning, "Name is empty.");
			if (parent_id != null) {
				try {
					parent = groups.get(Integer.parseInt(parent_id));
					if (parent == null)
						throw new GeneralException(ServletManager.Code.ClientError, "This group does not exist");
					infra = parent.getInfra();
					Group newGroup = Group.createGroup(name, parent, infra, sm);
					groups.put(newGroup.getSingleId(), newGroup);
					sm.setResponse(ServletManager.Code.Success, newGroup.getJSONString());
				} catch (NumberFormatException e) {
					throw new GeneralException(ServletManager.Code.ClientError, e);
				}
			} else
				throw new GeneralException(ServletManager.Code.ClientError, "Parent cannot be null");
		} catch (GeneralException e) {
			sm.setResponse(e);
		}
		sm.sendResponse();
	}

}
