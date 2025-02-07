package com.Ease.Dashboard.User;

import java.security.SecureRandom;

import com.Ease.Utils.*;
import org.apache.tomcat.util.codec.binary.Base64;

import com.Ease.Utils.Crypto.AES;
import com.Ease.Utils.Crypto.Hashing;

public class SessionSave {
    public enum SessionSaveData {
        NOTHING,
        ID,
        SESSIONID,
        TOKEN,
        KEYUSER,
        SALTUSER,
        USER,
        DATE
    }

    public static SessionSave loadSessionSave(String sessionId, String token, ServletManager sm) throws GeneralException, HttpServletException {
        DataBaseConnection db = sm.getDB();
        DatabaseRequest request = db.prepareRequest("SELECT * FROM savedSessions WHERE sessionId = ?;");
        request.setString(sessionId);
        DatabaseResult rs = request.get();
        if (rs.next()) {
            String hashedToken = rs.getString(SessionSaveData.TOKEN.ordinal());
            String saltKeyUser = rs.getString(SessionSaveData.SALTUSER.ordinal());
            String cryptedKeyUser = rs.getString(SessionSaveData.KEYUSER.ordinal());
            String userId = rs.getString(SessionSaveData.USER.ordinal());
            String keyUser;
            if (!Hashing.compare(token, hashedToken)) {
                throw new GeneralException(ServletManager.Code.ClientError, "Wrong token.");
            }
            keyUser = AES.decryptUserKey(cryptedKeyUser, token, saltKeyUser);
            SessionSave sessionSave = new SessionSave(saltKeyUser, token, sessionId, keyUser, userId);
            return sessionSave;
        } else {
            throw new GeneralException(ServletManager.Code.ClientError, "Wrong session id.");
        }
    }

    public static SessionSave createSessionSave(String keyUser, String userId, DataBaseConnection db) throws GeneralException {
        String cryptedKeyUser;
        String hashedToken;
        String token = tokenGenerator();
        String sessionId = sessionIdGenerator();
        String saltKeyUser;
        if ((saltKeyUser = AES.generateSalt()) == null) {
            throw new GeneralException(ServletManager.Code.InternError, "Can't create salt.");
        } else if ((cryptedKeyUser = AES.encryptUserKey(keyUser, token, saltKeyUser)) == null) {
            throw new GeneralException(ServletManager.Code.InternError, "Can't encrypt key.");
        } else if ((hashedToken = Hashing.hash(token)) == null) {
            throw new GeneralException(ServletManager.Code.InternError, "Can't hash token.");
        }
        DatabaseRequest request = db.prepareRequest("INSERT INTO savedSessions VALUES (NULL, ?, ?, ?, ?, ?, DEFAULT);");
        request.setString(sessionId);
        request.setString(hashedToken);
        request.setString(cryptedKeyUser);
        request.setString(saltKeyUser);
        request.setInt(userId);
        request.set();
        SessionSave sessionSave = new SessionSave(saltKeyUser, token, sessionId, keyUser, userId);
        return sessionSave;
    }

    private String saltKeyUser;
    private String token;
    private String sessionId;
    private String keyUser;
    private String userId;

    //Create a new session save
    public SessionSave(String saltKeyUser, String token, String sessionId, String keyUser, String userId) {
        this.saltKeyUser = saltKeyUser;
        this.token = token;
        this.sessionId = sessionId;
        this.keyUser = keyUser;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getKeyUser() {
        return keyUser;
    }

    public String getToken() {
        return token;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void eraseFromDB(DataBaseConnection db) throws GeneralException {
        DatabaseRequest request = db.prepareRequest("DELETE FROM savedSessions WHERE sessionId = ?;");
        request.setString(sessionId);
        request.set();
    }

    public static String tokenGenerator() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[42];
        random.nextBytes(bytes);
        return new Base64().encodeToString(bytes);
    }

    public static String sessionIdGenerator() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[42];
        random.nextBytes(bytes);
        return new Base64().encodeToString(bytes);
    }

}
