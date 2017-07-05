package com.Ease.Servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.Ease.Context.Group.Group;
import com.Ease.Context.Group.GroupManager;
import com.Ease.Dashboard.User.User;
import com.Ease.Utils.GeneralException;
import com.Ease.Utils.Regex;
import com.Ease.Utils.ServletManager;

/**
 * Servlet implementation class NewUser
 */
@WebServlet("/RegistrationEdhec")
public class RegistrationEdhec extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RegistrationEdhec() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		RequestDispatcher rd = request.getRequestDispatcher("index.jsp");
		rd.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) (session.getAttribute("user"));
		ServletManager sm = new ServletManager(this.getClass().getName(), request, response, true);
		try {
			String invitationCode = sm.getServletParam("invitationCode", false);
			String fname = sm.getServletParam("fname", true);
			String email = sm.getServletParam("email", true);
			String password = sm.getServletParam("password", false);
			String confirmPassword = sm.getServletParam("confirmPassword", false);

			if (user != null)
				Logout.logoutUser(user, sm); //throw new GeneralException(ServletManager.Code.ClientWarning, "You are logged on Ease.");
			if (fname == null || fname.length() < 2)
				throw new GeneralException(ServletManager.Code.ClientWarning, "Your name is too short.");
			else if (email == null || Regex.isEmail(email) == false)
				throw new GeneralException(ServletManager.Code.ClientWarning, "Incorrect email format.");
			else if (password == null || Regex.isPassword(password) == false)
				throw new GeneralException(ServletManager.Code.ClientWarning, "Password is too short (at least 8 characters).");
			else if (confirmPassword == null || password.equals(confirmPassword) == false)
				throw new GeneralException(ServletManager.Code.ClientWarning, "Passwords are not the same.");
			else {
				User newUser = User.createUser(email, fname, confirmPassword, sm.getServletContext(), sm.getDB());
				GroupManager groupManager = (GroupManager)sm.getContextAttr("groupManager");
				Group edhecGroup = groupManager.getGroupFromDBid("22");
				edhecGroup.addUser(newUser.getEmail(), newUser.getFirstName(), false, sm);
				newUser.getGroups().add(edhecGroup);
				session.setAttribute("user", newUser);
				sm.setResponse(ServletManager.Code.Success, "Registered successfully");
			}
		} catch (GeneralException e) {
			sm.setResponse(e);
		} catch (Exception e) {
			sm.setResponse(e);
		}
		sm.sendResponse();
	}
}