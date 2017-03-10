package com.Ease.Dashboard.App;

import org.json.simple.JSONObject;

import com.Ease.Utils.DataBaseConnection;
import com.Ease.Utils.DatabaseRequest;
import com.Ease.Utils.DatabaseResult;
import com.Ease.Utils.GeneralException;
import com.Ease.Utils.ServletManager;

public class AppInformation {
	
	public enum Data {
		NOTHING,
		ID,
		NAME
	}
	
	public static AppInformation createAppInformation(String name, ServletManager sm) throws GeneralException {
		DataBaseConnection db = sm.getDB();
		int db_id = db.set("INSERT INTO appsInformations values (null, '" + name + "');");
		return new AppInformation(String.valueOf(db_id), name);
	}
	
	public static String createAppInformationForUnconnected(String name, ServletManager sm) throws GeneralException {
		DataBaseConnection db = sm.getDB();
		int db_id = db.set("INSERT INTO appInformations values (null, '" + name + "');");
		return String.valueOf(db_id);
	}
	
	public static AppInformation loadAppInformation(String db_id, DataBaseConnection db) throws GeneralException {
		DatabaseRequest request = db.prepareRequest("SELECT * FROM appsInformations WHERE id = ? ;");
		request.setInt(db_id);
		DatabaseResult rs = request.get();
		String name;
		if (rs.next()) {
			name = rs.getString(Data.NAME.ordinal());
			return new AppInformation(db_id, name);
		} else
			throw new GeneralException(ServletManager.Code.InternError, "No app information");
	}
	
	protected String db_id;
	protected String name;
	
	public AppInformation(String db_id, String name) {
		this.db_id = db_id;
		this.name = name;
	}
	
	public String getDb_id() {
		return this.db_id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name, ServletManager sm) throws GeneralException {
		DataBaseConnection db = sm.getDB();
		db.set("UPDATE appsInformations SET name='" + name + "' WHERE id=" + this.db_id + ";");
		this.name = name;
	}
	
	public void removeFromDb(ServletManager sm) throws GeneralException {
		DataBaseConnection db = sm.getDB();
		db.set("DELETE FROM appsInformations WHERE id=" + this.db_id + ";");
	}

	public JSONObject getJson() {
		JSONObject res = new JSONObject();
		res.put("name", this.name);
		return res;
	}
}
