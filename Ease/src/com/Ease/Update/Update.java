package com.Ease.Update;

import java.util.LinkedList;
import java.util.List;

import com.Ease.Utils.*;
import org.json.simple.JSONObject;

import com.Ease.Dashboard.User.User;

import javax.servlet.ServletContext;

public class Update {
	
	public enum Data {
		NOTHING,
		ID,
		USER_ID,
		TYPE
	}
	
	public static List<Update> loadUpdates(User user, ServletContext context, DataBaseConnection db) throws GeneralException {
		List<Update> updates = new LinkedList<Update>();
		try {
			DatabaseRequest request = db.prepareRequest("SELECT * FROM updates WHERE user_id = ? AND id NOT IN (SELECT update_id FROM updatesRemoved);");
			request.setInt(user.getDBid());
			DatabaseResult rs = request.get();
			String db_id;
			String type;
			Update update;
			while (rs.next()) {
				db_id = rs.getString(Data.ID.ordinal());
				type = rs.getString(Data.TYPE.ordinal());
				switch (type) {
				case "updateNewPassword":
					update = UpdateNewPassword.loadUpdateNewPassword(db_id, user, db);
					break;

				case "updateNewAccount":
					update = UpdateNewAccount.loadUpdateNewAccount(db_id, user, context, db);
					break;

				default:
					throw new GeneralException(ServletManager.Code.InternError, "No such type");
				}
				updates.add(update);
			}
			return updates;
		} catch(GeneralException e) {
			throw e;
		}
		
	}
	
	public static String createUpdate(User user, String type, DataBaseConnection db) throws GeneralException {
		DatabaseRequest request = db.prepareRequest("INSERT INTO updates values (null, ?, ?);");
		request.setInt(user.getDBid());
		request.setString(type);
		return request.set().toString();
	}
	
	protected String db_id;
	protected String type;
	protected int single_id;
	protected User user;
	
	public Update(String db_id, int single_id, User user) {
		this.db_id = db_id;
		this.type = "Update";
		this.user = user;
		this.single_id = single_id;
	}
	
	public String getDbId() {
		return this.db_id;
	}
	
	public String getType() {
		return type;
	}
	
	public Integer getSingledId() {
		return this.single_id;
	}
	
	public void deleteFromDb(DataBaseConnection db) throws GeneralException {
		DatabaseRequest request = db.prepareRequest("DELETE FROM updates WHERE id = ?;");
		request.setInt(db_id);
		request.set();
	}
	
	public void reject(ServletManager sm) throws GeneralException {
		DataBaseConnection db = sm.getDB();
		DatabaseRequest request = db.prepareRequest("INSERT INTO updatesRemoved values (null, ?);");
		request.setInt(db_id);
		request.set();
	}
	
	public JSONObject getJson() throws GeneralException {
		throw new GeneralException(ServletManager.Code.InternError, "GetJson on an update... dufuk?");
	}

	public boolean matchJson(JSONObject json, User user) throws GeneralException {
		throw new GeneralException(ServletManager.Code.InternError, "You shouldn't be there");
	}
}
