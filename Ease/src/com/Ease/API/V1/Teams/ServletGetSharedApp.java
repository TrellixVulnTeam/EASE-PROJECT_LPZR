package com.Ease.API.V1.Teams;

import com.Ease.Dashboard.App.App;
import com.Ease.Dashboard.App.SharedApp;
import com.Ease.Dashboard.App.WebsiteApp.ClassicApp.ClassicApp;
import com.Ease.Team.Team;
import com.Ease.Team.TeamManager;
import com.Ease.Team.TeamUser;
import com.Ease.Utils.Servlets.GetServletManager;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by thomas on 29/05/2017.
 */
@WebServlet("/api/v1/teams/GetSharedApp")
public class ServletGetSharedApp extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GetServletManager sm = new GetServletManager(this.getClass().getName(), request, response, true);
        try {
            Integer team_id = sm.getIntParam("team_id", true);
            sm.needToBeTeamUserOfTeam(team_id);
            /* @TODO For the moment we use single_id but it will be replaced by db_id in the future */
            Integer app_id = sm.getIntParam("app_id", true);
            Integer teamUser_id = sm.getIntParam("teamUser_id", true);
            TeamManager teamManager = (TeamManager) sm.getContextAttr("teamManager");
            Team team = teamManager.getTeamWithId(team_id);
            TeamUser teamUser = sm.getTeamUserForTeam(team);
            TeamUser sharedApp_owner = team.getTeamUserWithId(teamUser_id);
            SharedApp sharedApp = sharedApp_owner.getSharedAppWithId(app_id);
            App app = (App) sharedApp;
            App shareableApp = (App) sharedApp.getHolder();
            if (teamUser.isTeamAdmin() || sharedApp.getTeamUser_tenant() == sharedApp_owner) {
                if (app.isClassicApp()) {
                    if (shareableApp.isClassicApp() || (shareableApp.isEmpty() && (sharedApp.adminHasAccess() && teamUser.isTeamAdmin()) || (teamUser == sharedApp_owner)))
                        ((ClassicApp) app).getAccount().decipherWithTeamKeyIfNeeded(teamUser.getDeciphered_teamKey());
                }

            }
            sm.setSuccess(shareableApp.getShareableJson());
        } catch (Exception e) {
            sm.setError(e);
        }
        sm.sendResponse();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher("index.jsp");
        rd.forward(request, response);
    }
}
