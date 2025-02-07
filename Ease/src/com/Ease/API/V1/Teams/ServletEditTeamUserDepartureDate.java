package com.Ease.API.V1.Teams;

import com.Ease.Team.Team;
import com.Ease.Team.TeamManager;
import com.Ease.Team.TeamUser;
import com.Ease.Utils.HttpServletException;
import com.Ease.Utils.HttpStatus;
import com.Ease.Utils.Servlets.PostServletManager;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by thomas on 02/06/2017.
 */
@WebServlet("/api/v1/teams/EditTeamUserDepartureDate")
public class ServletEditTeamUserDepartureDate extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PostServletManager sm = new PostServletManager(this.getClass().getName(), request, response, true);
        try {

            Integer team_id = sm.getIntParam("team_id", true);
            sm.needToBeAdminOfTeam(team_id);
            TeamManager teamManager = (TeamManager) sm.getContextAttr("teamManager");
            Team team = teamManager.getTeamWithId(team_id);
            TeamUser teamUser = sm.getTeamUserForTeam(team);
            Integer teamUser_id = sm.getIntParam("team_user_id", true);
            TeamUser teamUser_to_modify = team.getTeamUserWithId(teamUser_id);
            if (!(teamUser.isSuperior(teamUser_to_modify) || teamUser == teamUser_to_modify))
                throw new HttpServletException(HttpStatus.Forbidden, "You cannot do this dude.");
            String departureDateString = sm.getStringParam("departure_date", true);
            if (departureDateString == null || departureDateString.equals(""))
                teamUser_to_modify.setDepartureDate(null);
            else {
                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date departure_date = dateFormat.parse(departureDateString);
                    if (departure_date.getTime() <= sm.getTimestamp().getTime())
                        throw new HttpServletException(HttpStatus.BadRequest, "Please, provide a valid departure date.");
                    teamUser_to_modify.setDepartureDate(departure_date);
                } catch (ParseException e) {
                    throw new HttpServletException(HttpStatus.InternError, "Wrong date format.");
                }

            }
            sm.saveOrUpdate(teamUser_to_modify);
            sm.setSuccess("Departure date edited.");
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
