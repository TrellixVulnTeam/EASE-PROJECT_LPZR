package com.Ease.API.V1.Common;

import com.Ease.Dashboard.User.User;
import com.Ease.Hibernate.HibernateQuery;
import com.Ease.Mail.MailJetBuilder;
import com.Ease.Utils.GeneralException;
import com.Ease.Utils.HttpServletException;
import com.Ease.Utils.HttpStatus;
import com.Ease.Utils.Regex;
import com.Ease.Utils.Servlets.PostServletManager;
import com.mailjet.client.resource.ContactslistManageContact;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/v1/common/Registration")
public class ServletRegistration extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PostServletManager sm = new PostServletManager(this.getClass().getName(), request, response, true);
        try {
            User user = sm.getUser();
            if (user != null)
                user.logoutFromSession(sm.getSession().getId(), sm.getServletContext(), sm.getDB());
            String username = sm.getStringParam("username", true);
            String email = sm.getStringParam("email", true);
            String password = sm.getStringParam("password", false);
            String digits = sm.getStringParam("digits", false);
            String code = sm.getStringParam("code", false);
            Long registration_date = sm.getLongParam("registration_date", true);
            Boolean send_news = sm.getBooleanParam("newsletter", true);
            if (username == null || username.length() < 2 || username.length() > 30)
                throw new HttpServletException(HttpStatus.BadRequest, "Invalid username");
            if (email == null || !Regex.isEmail(email))
                throw new HttpServletException(HttpStatus.BadRequest, "Invalid email");
            if (password == null || !Regex.isPassword(password))
                throw new HttpServletException(HttpStatus.BadRequest, "Invalid password");
            if (registration_date == null)
                throw new HttpServletException(HttpStatus.BadRequest, "Invalid registration date");
            if ((digits == null || digits.length() != 6) && (code == null || code.equals("")))
                throw new HttpServletException(HttpStatus.BadRequest, "Missing parameter digits or code");
            if (send_news == null)
                throw new HttpServletException(HttpStatus.BadRequest, "Invalid newsletter param");
            HibernateQuery hibernateQuery = sm.getHibernateQuery();
            if (code != null && !code.equals("")) {
                hibernateQuery.querySQLString("SELECT code FROM pendingTeamInvitations JOIN teamUsers ON pendingTeamInvitations.teamUser_id = teamUsers.id WHERE teamUsers.email = ?");
                hibernateQuery.setParameter(1, email);
                String valid_code = (String) hibernateQuery.getSingleResult();
                if (valid_code == null)
                    throw new HttpServletException(HttpStatus.BadRequest, "No invitation for this email.");
                if (!valid_code.equals(code))
                    throw new HttpServletException(HttpStatus.BadRequest, "Invalid code.");
            } else {
                hibernateQuery.querySQLString("SELECT digits FROM userPendingRegistrations WHERE email = ?");
                hibernateQuery.setParameter(1, email);
                String db_digits = (String) hibernateQuery.getSingleResult();
                if (db_digits == null || db_digits.equals(""))
                    throw new HttpServletException(HttpStatus.BadRequest, "You didn't ask for an account.");
                if (!db_digits.equals(digits))
                    throw new HttpServletException(HttpStatus.BadRequest, "Invalid digits.");
            }
            User newUser = User.createUser(email, username, password, registration_date, sm.getServletContext(), sm.getDB());
            if (send_news) {
                MailJetBuilder mailJetBuilder = new MailJetBuilder(ContactslistManageContact.resource, 13300);
                mailJetBuilder.property(ContactslistManageContact.EMAIL, newUser.getEmail());
                mailJetBuilder.property(ContactslistManageContact.NAME, newUser.getFirstName());
                mailJetBuilder.property(ContactslistManageContact.ACTION, "addnoforce");
                mailJetBuilder.post();
            }
            sm.setUser(newUser);
            ((Map<String, User>) sm.getContextAttr("users")).put(email, newUser);
            ((Map<String, User>) sm.getContextAttr("sessionIdUserMap")).put(sm.getSession().getId(), newUser);
            ((Map<String, User>) sm.getContextAttr("sIdUserMap")).put(newUser.getSessionSave().getSessionId(), newUser);
            sm.setSuccess(newUser.getJson());
        } catch (GeneralException e) {
            sm.setError(new HttpServletException(HttpStatus.BadRequest, e.getMsg()));
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
