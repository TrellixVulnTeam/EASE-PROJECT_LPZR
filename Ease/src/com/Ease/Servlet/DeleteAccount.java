package com.Ease.Servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.Ease.Dashboard.User.User;
import com.Ease.Utils.GeneralException;
import com.Ease.Utils.ServletManager;

/**
 * Servlet implementation class DeleteAccount
 */
@WebServlet("/DeleteAccount")
public class DeleteAccount extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteAccount() {
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
        HttpSession session = request.getSession();
        User user = (User) (session.getAttribute("user"));
        ServletManager sm = new ServletManager(this.getClass().getName(), request, response, true);

        try {
            sm.needToBeConnected();
            String password = sm.getServletParam("password", false);
            if (password == null || password.equals(""))
                throw new GeneralException(ServletManager.Code.ClientWarning, "Password does not match");
            user.getKeys().isGoodPassword(password);
            user.deleteFromDb(sm);
            user.deconnect(sm);
            sm.setResponse(ServletManager.Code.Success, "Account deleted");
        } catch (GeneralException e) {
            sm.setResponse(e);
        } catch (Exception e) {
            sm.setResponse(e);
        }
        sm.sendResponse();
        session.invalidate();
    }

}
