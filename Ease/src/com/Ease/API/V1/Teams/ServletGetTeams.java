package com.Ease.API.V1.Teams;

import com.Ease.Team.TeamUser;
import com.Ease.Utils.Servlets.GetServletManager;
import org.json.simple.JSONArray;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by thomas on 05/05/2017.
 */
@WebServlet("/api/v1/teams/GetTeams")
public class ServletGetTeams extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher("index.jsp");
        rd.forward(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GetServletManager sm = new GetServletManager(this.getClass().getName(), request, response, true);
        try {
            sm.needToBeTeamUser();
            List<TeamUser> teamUserList = sm.getTeamUsers();
            JSONArray res = new JSONArray();
            for (TeamUser teamUser : teamUserList)
                res.add(teamUser.getTeam().getSimpleJson());
            sm.setSuccess(res);
        } catch (Exception e) {
            sm.setError(e);
        }
        sm.sendResponse();
    }
}
