package com.Ease.API.V1.Teams;

import com.Ease.Dashboard.App.App;
import com.Ease.Dashboard.App.ShareableApp;
import com.Ease.Dashboard.App.SharedApp;
import com.Ease.Team.Channel;
import com.Ease.Team.Team;
import com.Ease.Team.TeamManager;
import com.Ease.Team.TeamUser;
import com.Ease.Utils.*;
import com.Ease.Utils.Servlets.PostServletManager;
import org.json.simple.JSONObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by thomas on 30/05/2017.
 */
@WebServlet("/api/v1/teams/ShareApp")
public class ServletShareApp extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PostServletManager sm = new PostServletManager(this.getClass().getName(), request, response, true);
        try {
            Integer team_id = sm.getIntParam("team_id", true);
            sm.needToBeTeamUserOfTeam(team_id);
            TeamManager teamManager = (TeamManager) sm.getContextAttr("teamManager");
            Team team = teamManager.getTeamWithId(team_id);
            TeamUser teamUser_owner = sm.getTeamUserForTeamId(team_id);
            Integer teamUser_tenant_id = sm.getIntParam("team_user_id", true);
            TeamUser teamUser_tenant = team.getTeamUserWithId(teamUser_tenant_id);
            Integer app_id = sm.getIntParam("app_id", true);
            ShareableApp shareableApp = team.getAppManager().getShareableAppWithId(app_id);
            if (!(shareableApp.getTeamUser_owner() == teamUser_owner) && !teamUser_owner.isTeamAdmin())
                throw new GeneralException(ServletManager.Code.ClientError, "You cannot access this app");
            if (shareableApp.getTeamUser_tenants().contains(teamUser_tenant))
                throw new HttpServletException(HttpStatus.BadRequest, "You already shared this app to this user");
            Channel channel = shareableApp.getChannel();
            JSONObject params = shareableApp.getNeededParams(sm);
            DataBaseConnection db = sm.getDB();
            int transaction = db.startTransaction();
            if (shareableApp.getPendingTeamUsers().contains(teamUser_tenant)) {
                shareableApp.removePendingTeamUser(teamUser_tenant, db);
                teamUser_tenant.addNotification(teamUser_owner.getUsername() + " approved your access to " + ((App) shareableApp).getName() + ((channel == null) ? "" : " in " + channel.getName()), sm.getTimestamp());
            }
            SharedApp sharedApp = shareableApp.share(teamUser_owner, teamUser_tenant, channel, team, params, sm);
            if (teamUser_tenant == sm.getTeamUserForTeam(team))
                sharedApp.accept(db);
            db.commitTransaction(transaction);
            shareableApp.addSharedApp(sharedApp);
            team.getAppManager().addSharedApp(sharedApp);
            teamUser_tenant.addNotification(teamUser_owner.getUsername() + " sent you " + ((App) shareableApp).getName() + " in " + (shareableApp.getChannel() == null ? "your Personal Space" : shareableApp.getChannel().getName()), sm.getTimestamp());
            sm.setSuccess(sharedApp.getSharedJSON());
        } catch (Exception e) {
            sm.setError(e);
        }
        sm.sendResponse();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher("index.jsp");
        rd.forward(request, response);
    }
}
