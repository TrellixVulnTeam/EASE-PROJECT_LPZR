package com.Ease.Utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.mail.MessagingException;

import com.Ease.Context.Group.Group;
import com.Ease.Context.Group.GroupManager;
import com.Ease.Utils.Crypto.CodeGenerator;

public class Invitation {

	public static List<Group> verifyInvitation(String email, String invitationCode, ServletManager sm) throws GeneralException {
		DataBaseConnection db = sm.getDB();
		List<Group> groups = new LinkedList<Group>();
		GroupManager groupManager = (GroupManager) sm.getContextAttr("groupManager");
		System.out.println(invitationCode);
		System.out.println(email);
		ResultSet rs = db.get("SELECT id FROM invitations WHERE email='" + email + "' AND linkCode='" + invitationCode + "';");
		try {
			if (rs.next()) {
				String id = rs.getString(1);
				ResultSet rs2 = db.get("SELECT group_id FROM invitationsAndGroupsMap WHERE invitation_id=" + id + ";");
				while (rs2.next()) {
					groups.add(groupManager.getGroupFromDBid(rs2.getString(1)));
				}
				int transaction = db.startTransaction();
				db.set("DELETE FROM invitationsAndGroupsMap WHERE invitation_id=" + id + ";");
				db.set("DELETE FROM invitations WHERE id=" + id + ";");
				db.commitTransaction(transaction);
			}
			System.out.println("NBR GROUP: " + groups.size());
			return groups;
		} catch (SQLException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		}
	}

	public static void sendInvitation(String email, String name, Group group, ServletManager sm) throws GeneralException {
		DataBaseConnection db = sm.getDB();
		String invitationCode = CodeGenerator.generateNewCode();
		try {
			ResultSet rs = db.get("SELECT * FROM invitations WHERE email='" + email + "';");
			if (rs.next()) {
				db.set("UPDATE invitations SET linkCode='" + invitationCode + "' where email='" + email + "';");
			} else {
				String db_id = db.set("INSERT INTO invitations values(NULL, '" + email + "', '" + invitationCode + "');").toString();
				if (group != null)
					db.set("INSERT INTO invitationsAndGroupsMap values(NULL, " + db_id + ", " + group.getDBid() + ");");
			}
			Mail mailToSend;
			mailToSend = new Mail();
			mailToSend.sendInvitationEmail(email, name, invitationCode);
		} catch (MessagingException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		} catch (SQLException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		}
	}
}
