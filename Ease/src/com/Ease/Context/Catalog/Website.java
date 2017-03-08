package com.Ease.Context.Catalog;

import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.Ease.Context.Variables;
import com.Ease.Context.Group.Group;
import com.Ease.Dashboard.User.User;
import com.Ease.Utils.DataBaseConnection;
import com.Ease.Utils.GeneralException;
import com.Ease.Utils.IdGenerator;
import com.Ease.Utils.ServletManager;
import com.Ease.Utils.Crypto.RSA;

public class Website {
	public enum WebsiteData {
		NOTHING,
		ID,
		LOGIN_URL,
		NAME,
		FOLDER,
		SSO,
		NO_LOGIN,
		WEBSITE_HOMEPAGE,
		RATIO,
		POSITION,
		WEBSITE_ATTRIBUTES_ID
	}
	
	private static String last_db_id = "0";

	public static List<Website> loadNewWebsites(Map<String, Sso> ssoDbIdMap, ServletManager sm) throws GeneralException {
		List<Website> newWebsites = new LinkedList<Website>();
		DataBaseConnection db = sm.getDB();
		ResultSet rs = db.get("SELECT * FROM websites WHERE id > " + last_db_id + ";");
		try {
			while (rs.next()) {
					String db_id = rs.getString(WebsiteData.ID.ordinal());
					if (Integer.parseInt(db_id) > Integer.parseInt(last_db_id))
						last_db_id = db_id;
					List<WebsiteInformation> website_informations = WebsiteInformation.loadInformations(db_id, db);
					String loginUrl = rs.getString(WebsiteData.LOGIN_URL.ordinal());
					String name = rs.getString(WebsiteData.NAME.ordinal());
					String folder = rs.getString(WebsiteData.FOLDER.ordinal());
					Sso sso = ssoDbIdMap.get(rs.getString(WebsiteData.SSO.ordinal()));
					boolean noLogin = rs.getBoolean(WebsiteData.NO_LOGIN.ordinal());
					String website_homepage = rs.getString(WebsiteData.WEBSITE_HOMEPAGE.ordinal());
					int ratio = rs.getInt(WebsiteData.RATIO.ordinal());
					int position = rs.getInt(WebsiteData.POSITION.ordinal());
					String websiteAttributesId = rs.getString(WebsiteData.WEBSITE_ATTRIBUTES_ID.ordinal());
					WebsiteAttributes websiteAttributes = null;
					if (websiteAttributesId != null)
						websiteAttributes = WebsiteAttributes.loadWebsiteAttributes(websiteAttributesId, db);
					int single_id = ((IdGenerator)sm.getContextAttr("idGenerator")).getNextId();
					Website site = new Website(db_id, single_id, name, loginUrl, folder, sso, noLogin, website_homepage, ratio, position, website_informations, websiteAttributes);
					newWebsites.add(site);
					if (sso != null)
						sso.addWebsite(site);
					site.loadGroupIds(db);
			}
		} catch (SQLException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		}
		return newWebsites;
	}
	
	public static Website getWebsite(int single_id, ServletManager sm) throws GeneralException {
		@SuppressWarnings("unchecked")
		Map<Integer, Website> websitesMap = (Map<Integer, Website>)sm.getContextAttr("websites");
		Website site = websitesMap.get(single_id);
		if (site == null)
			throw new GeneralException(ServletManager.Code.InternError, "This website dosen't exist!");
		return site;
	}

	public static Website createWebsite(String url, String name, String homePage, String folder, boolean haveLoginButton, boolean noLogin, String[] haveLoginWith, String[] infoNames, String[] infoTypes, String[] placeholders, String[] placeholderIcons, Catalog catalog, ServletManager sm) throws GeneralException {
		DataBaseConnection db = sm.getDB();
		ResultSet rs = db.get("SELECT * FROM websites WHERE folder = '"+ folder+"' AND website_name='"+name+"';");
		try {
			if (rs.next()){
				throw new GeneralException(ServletManager.Code.UserMiss, "This website already exists");
			}
			int transaction  = db.startTransaction();
			WebsiteAttributes attributes = WebsiteAttributes.createWebsiteAttributes(db);

			String db_id = db.set("INSERT INTO websites VALUES (null, '"+ url +"', '"+ name +"', '" + folder + "', NULL, " + (noLogin ? "1" : "0") + ", '"+ homePage +"', 0, 1, "+ attributes.getDbId() +");").toString();
			last_db_id = db_id;

			List<WebsiteInformation> infos = new LinkedList<WebsiteInformation>();
			for(int i=0; i < infoNames.length; i++) {
				infos.add(WebsiteInformation.createInformation(db_id, infoNames[i], infoTypes[i], String.valueOf(i), placeholders[i], placeholderIcons[i], db));
			}

			if(haveLoginButton){
				db.set("INSERT INTO loginWithWebsites VALUES (null, "+ db_id +");");
			}

			List<Website> loginWithWebsites = new LinkedList<Website>();
			if(haveLoginWith != null){
				for(int i=0;i<haveLoginWith.length;i++){
					ResultSet rs2 = db.get("SELECT id FROM loginWithWebsites WHERE website_id = "+haveLoginWith[i]+";");
					if(rs2.next()){
						String id = db.set("INSERT INTO websitesLogWithMap VALUES (null, "+db_id+", "+rs2.getString(1)+");").toString();
					}
					loginWithWebsites.add(catalog.getWebsiteWithDBid(haveLoginWith[i]));
				}
			}
			IdGenerator idGenerator = (IdGenerator)sm.getContextAttr("idGenerator");
			db.commitTransaction(transaction);
			return new Website(db_id, idGenerator.getNextId(), name, url, folder, null, false, homePage, 0, 1, infos, attributes, loginWithWebsites);
		} catch (SQLException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		}
	}


	public static List<Website> loadWebsites(DataBaseConnection db, Map<String, Sso> ssoDbIdMap, ServletContext context) throws GeneralException {
		try {
			List<Website> websites = new LinkedList<Website>();
			ResultSet rs = db.get("SELECT websites.* FROM websites LEFT JOIN websiteAttributes ON (website_attributes_id = websiteAttributes.id) ORDER BY new, ratio");
			while (rs.next()) {
				String db_id = rs.getString(WebsiteData.ID.ordinal());
				if (Integer.parseInt(db_id) > Integer.parseInt(last_db_id))
					last_db_id = db_id;
				List<WebsiteInformation> website_informations = WebsiteInformation.loadInformations(db_id, db);
				String loginUrl = rs.getString(WebsiteData.LOGIN_URL.ordinal());
				String name = rs.getString(WebsiteData.NAME.ordinal());
				String folder = rs.getString(WebsiteData.FOLDER.ordinal());
				Sso sso = ssoDbIdMap.get(rs.getString(WebsiteData.SSO.ordinal()));
				boolean noLogin = rs.getBoolean(WebsiteData.NO_LOGIN.ordinal());
				String website_homepage = rs.getString(WebsiteData.WEBSITE_HOMEPAGE.ordinal());
				int ratio = rs.getInt(WebsiteData.RATIO.ordinal());
				int position = rs.getInt(WebsiteData.POSITION.ordinal());
				String websiteAttributesId = rs.getString(WebsiteData.WEBSITE_ATTRIBUTES_ID.ordinal());
				WebsiteAttributes websiteAttributes = null;
				if (websiteAttributesId != null)
					websiteAttributes = WebsiteAttributes.loadWebsiteAttributes(websiteAttributesId, db);
				int single_id = ((IdGenerator)context.getAttribute("idGenerator")).getNextId();
				Website site = new Website(db_id, single_id, name, loginUrl, folder, sso, noLogin, website_homepage, ratio, position, website_informations, websiteAttributes);
				websites.add(site);
				if (sso != null)
					sso.addWebsite(site);
				site.loadGroupIds(db);
			}
			return websites;
		} catch (SQLException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		}
	}

	///// Check if website exist when scrapp

	public static JSONArray existsInDb(String websiteHost, ServletManager sm) throws GeneralException{
		DataBaseConnection db = sm.getDB();
		Catalog catalog = (Catalog)sm.getContextAttr("catalog");
		ResultSet rs = db.get("select * from websites where noLogin=0 AND website_name NOT IN ('Google AdWords', 'Google Analytics', 'Google Play');");
		JSONArray result = new JSONArray();
		try {
			while(rs.next()){
				String loginUrl = rs.getString(WebsiteData.LOGIN_URL.ordinal());
				websiteHost = websiteHost.toLowerCase();
				loginUrl = loginUrl.toLowerCase();
				if(loginUrl.contains(websiteHost)){
					result.add(String.valueOf((catalog.getWebsiteWithDBid(rs.getString(WebsiteData.ID.ordinal())).getSingleId())));
				}
			}
		} catch (SQLException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		}
		return result;
	}

	public static String existsInDbFacebook(String appName, ServletManager sm) throws GeneralException{
		DataBaseConnection db = sm.getDB();
		Catalog catalog = (Catalog)sm.getContextAttr("catalog");
		ResultSet rs = db.get("select * from websites where id in (select website_id from websitesLogWithMap where website_logwith_id in (select id from loginWithWebsites where website_id in (select id from websites where website_name='Facebook')));");
		try {
			while(rs.next()){
				String name = rs.getString(WebsiteData.NAME.ordinal());
				appName = appName.toLowerCase();
				name = name.toLowerCase();
				if(appName.contains(name)){
					return String.valueOf((catalog.getWebsiteWithDBid(rs.getString(WebsiteData.ID.ordinal())).getSingleId()));
				}
			}
		} catch (SQLException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		}
		return null;
	}

	public static String existsInDbLinkedin(String appName, ServletManager sm) throws GeneralException{
		DataBaseConnection db = sm.getDB();
		Catalog catalog = (Catalog)sm.getContextAttr("catalog");
		ResultSet rs = db.get("select * from websites where id in (select website_id from websitesLogWithMap where website_logwith_id in (select id from loginWithWebsites where website_id in (select id from websites where website_name='Linkedin')));");
		try {
			while(rs.next()){
				String name = rs.getString(WebsiteData.NAME.ordinal());
				appName = appName.toLowerCase();
				name = name.toLowerCase();
				if(appName.contains(name)){
					return String.valueOf((catalog.getWebsiteWithDBid(rs.getString(WebsiteData.ID.ordinal())).getSingleId()));
				}
			}
		} catch (SQLException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		}
		return null;
	}

	//// -------

	protected String db_id;
	protected int single_id;
	protected String name;
	protected String loginUrl;
	protected String folder;
	protected int position;
	protected Sso sso;
	protected boolean noLogin;
	protected String website_homepage;
	protected int ratio;
	protected WebsiteAttributes websiteAttributes;
	protected List<WebsiteInformation> website_informations;
	protected List<Website> loginWithWebsites;
	protected List<String> groupIds;

	public Website(String db_id, int single_id, String name, String loginUrl, String folder, Sso sso, boolean noLogin, String website_homepage, int ratio, int position, List<WebsiteInformation> website_informations, WebsiteAttributes websiteAttributes) {
		this.db_id = db_id;
		this.single_id = single_id;
		this.loginUrl = loginUrl;
		this.folder = folder;
		this.sso = sso;
		this.noLogin = noLogin;
		this.website_homepage = website_homepage;
		this.ratio = ratio;
		this.website_informations = website_informations;
		this.name = name;
		this.position = position;
		this.loginWithWebsites = new LinkedList<Website>();
		this.websiteAttributes = websiteAttributes;
		this.groupIds = new LinkedList<String>();
	}

	public Website(String db_id, int single_id, String name, String loginUrl, String folder, Sso sso, boolean noLogin, String website_homepage, int ratio, int position, List<WebsiteInformation> website_informations, WebsiteAttributes websiteAttributes, List<Website> loginWithWebsites) {
		this.db_id = db_id;
		this.single_id = single_id;
		this.loginUrl = loginUrl;
		this.folder = folder;
		this.sso = sso;
		this.noLogin = noLogin;
		this.website_homepage = website_homepage;
		this.ratio = ratio;
		this.website_informations = website_informations;
		this.name = name;
		this.position = position;
		this.loginWithWebsites = loginWithWebsites;
		this.websiteAttributes = websiteAttributes;
		this.groupIds = new LinkedList<String>();
	}
	
	public void loadGroupIds(DataBaseConnection db) throws GeneralException {
		ResultSet rs = db.get("SELECT group_id FROM websitesAndGroupsMap JOIN groups ON (websitesAndGroupsMap.group_id = groups.id) WHERE website_id = " + this.db_id + ";");
		try {
			while(rs.next()) {
				String parent_id = rs.getString(1);
				this.groupIds.add(parent_id);
				this.loadSubGroupIds(parent_id, db);
			}
		} catch (SQLException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		}
	}
	
	public void loadSubGroupIds(String parent_id, DataBaseConnection db) throws GeneralException {
		ResultSet rs = db.get("SELECT id FROM groups WHERE parent = " + parent_id + ";");
		try {
			while(rs.next()) {
				String newParent_id = rs.getString(1);
				this.groupIds.add(newParent_id);
				this.loadSubGroupIds(newParent_id, db);
			}
		} catch (SQLException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		}
	}

	public String getDb_id() {
		return this.db_id;
	}

	public List<WebsiteInformation> getInfos() {
		return website_informations;
	}

	public Map<String, String> getNeededInfos(ServletManager sm) throws GeneralException {
		Map<String, String> infos = new HashMap<String, String>();
		for (WebsiteInformation info : website_informations) {
			String info_name = info.getInformationName();
			String value = sm.getServletParam(info_name, false);
			if (value == null || value.isEmpty()) {
				throw new GeneralException(ServletManager.Code.ClientWarning, "Wrong info: " + info_name + ".");
			}
			if (info_name.equals("password")) {
				//Mettre un param keyDate dans le post si besoin de decrypter en RSA. Correspond à la private key RSA, 
				String keyDate = sm.getServletParam("keyDate", true);
				if (keyDate != null && !keyDate.equals("")) {
					value = RSA.Decrypt(value, Integer.parseInt(keyDate));
				}
				value = sm.getUser().encrypt(value);
			}
			infos.put(info_name, value);
		}
		return infos;
	}
	
	public Map<String, String> getNeededInfosForEdition(ServletManager sm) throws GeneralException {
		Map<String, String> infos = new HashMap<String, String>();
		for (WebsiteInformation info : website_informations) {
			String info_name = info.getInformationName();
			String value = sm.getServletParam(info_name, false);
			if (value == null || value.equals("")) {
				if (info_name.equals("password"))
					continue;
				else
					throw new GeneralException(ServletManager.Code.ClientWarning, "Wrong info: " + info_name + ".");
			}
			if (info_name.equals("password")) {
				//Mettre un param keyDate dans le post si besoin de decrypter en RSA. Correspond à la private key RSA, 
				String keyDate = sm.getServletParam("keyDate", true);
				if (keyDate != null && !keyDate.equals("")) {
					value = RSA.Decrypt(value, Integer.parseInt(keyDate));
				}
				value = sm.getUser().encrypt(value);
			}
			infos.put(info_name, value);
		}
		System.out.println(infos.size());
		return infos;
	}

	public void loadLoginWithWebsites(DataBaseConnection db, Catalog catalog) throws GeneralException {
		ResultSet rs = db.get("SELECT loginWithWebsites.website_id FROM loginWithWebsites JOIN websitesLogWithMap ON loginWithWebsites.id = website_logwith_id WHERE websitesLogWithMap.website_id=" + this.db_id + ";");
		try {
			while (rs.next()) {
				this.loginWithWebsites.add(catalog.getWebsiteWithDBid(rs.getString(1)));
			}
		} catch (SQLException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		}
	}

	public int getSingleId() {
		return this.single_id;
	}
	
	public void setSingleId(int singleId) {
		this.single_id = singleId;
	}

	public Sso getSso() {
		return this.sso;
	}
	
	public int getSsoId() {
		if (this.sso == null)
			return -1;
		else
			return this.sso.getSingleId();
	}

	public String getName() {
		return this.name;
	}

	public String getFolder() {
		return Variables.WEBSITES_PATH + this.folder +"/";
	}

	public String getAbsolutePath(){
		return Variables.PROJECT_PATH  + this.getFolder();
	}

	public String getUrl() {
		return this.loginUrl;
	}

	public String getHomePageUrl() {
		return this.website_homepage;
	}

	public String getLoginWith() {
		String res = "";
		Iterator<Website> it = this.loginWithWebsites.iterator();
		while (it.hasNext()) {
			res += it.next().getSingleId();
			if (it.hasNext())
				res += ",";
		}
		return res;
	}

	public List<WebsiteInformation> getInformations() {
		return this.website_informations;
	}

	public boolean isInPublicCatalog() {
		return this.websiteAttributes != null && this.groupIds.isEmpty();
	}

	public boolean noLogin() {
		return this.noLogin;
	}

	public boolean work() {
		if (this.websiteAttributes == null)
			return true;
		return this.websiteAttributes.isWorking();
	}

	public boolean isNew() {
		if (this.websiteAttributes == null)
			return false;
		return this.websiteAttributes.isNew();
	}

	public JSONObject getJSON(ServletManager sm) throws GeneralException{
		JSONParser parser = new JSONParser();
		try {
			JSONObject a = (JSONObject) parser.parse(new FileReader(this.getAbsolutePath() + "connect.json"));
			a.put("loginUrl",loginUrl);
			a.put("website_name", this.name);
			a.put("siteSrc", this.getFolder());
			a.put("img", Variables.URL_PATH + this.getFolder() + "logo.png");
			return a;
		} catch (IOException | ParseException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		}
	}

	public boolean loginUrlMatch(String url) {
		String[] loginUrlSplitted = this.loginUrl.split("\\/*\\/");
		return url.contains(loginUrlSplitted[1]);
	}
	
	public boolean homepageUrlMatch(String url) {
		String[] homepageUrlSplitted = this.website_homepage.split("\\/*\\/");
		return url.contains(homepageUrlSplitted[1]);
	}

	@SuppressWarnings("unchecked")
	public JSONObject getJsonForCatalog() {
		JSONObject res = new JSONObject();
		res.put("name", this.name);
		res.put("singleId", this.single_id);
		res.put("logo", Variables.WEBSITES_PATH + this.folder + "/" + "logo.png");
		JSONArray logWithWebsites = new JSONArray();
		for (Website logWithWebsite : this.loginWithWebsites)
			logWithWebsites.add(logWithWebsite.getSingleId());
		res.put("loginWith", logWithWebsites);
		if (this.sso != null)
			res.put("ssoId", this.sso.getSingleId());
		else
			res.put("ssoId", -1);
		res.put("url", this.website_homepage);
		JSONArray inputs = new JSONArray();
		for (WebsiteInformation websiteInformation : this.website_informations) {
			inputs.add(websiteInformation.getJson());
		}
		res.put("inputs", inputs);
		res.put("isNew", this.isNew());
		res.put("position", this.position);
		return res;
	}

	public void incrementRatio(DataBaseConnection db) throws GeneralException {
		this.ratio++;
		db.set("UPDATE websites SET ratio = ratio + 1 WHERE id = " + this.db_id + ";");
	}
	
	public void decrementRatio(DataBaseConnection db) throws GeneralException {
		if (this.ratio > 0 ) {
			this.ratio--;
			db.set("UPDATE websites SET ratio = ratio - 1 WHERE id = " + this.db_id + ";");
		}
	}

	public int getRatio() {
		return this.ratio;
	}

	public void refresh(ServletManager sm) throws GeneralException {
		DataBaseConnection db = sm.getDB();
		ResultSet rs = db.get("SELECT * FROM websites WHERE id = " + this.db_id  + ";");
		try {
			rs.next();
			this.loginUrl = rs.getString(WebsiteData.LOGIN_URL.ordinal());
			this.name = rs.getString(WebsiteData.NAME.ordinal());
			this.folder = rs.getString(WebsiteData.FOLDER.ordinal());
			this.noLogin = rs.getBoolean(WebsiteData.NO_LOGIN.ordinal());
			this.website_homepage = rs.getString(WebsiteData.WEBSITE_HOMEPAGE.ordinal());
			this.ratio = rs.getInt(WebsiteData.RATIO.ordinal());
			this.position = rs.getInt(WebsiteData.POSITION.ordinal());
			for (WebsiteInformation info : this.website_informations)
				info.refresh(sm);
			this.websiteAttributes.refresh(sm);
		} catch (SQLException e) {
			throw new GeneralException(ServletManager.Code.InternError, e);
		}
	}

	public boolean isInPublicCatalogForUser(User user) {
		for (Group group : user.getGroups()) {
			if (this.groupIds.contains(group.getDBid()))
				return true;
		}
		return this.groupIds.isEmpty();
	}
}
