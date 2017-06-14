package com.Ease.Team;

import com.Ease.Notification.ChannelNotification;
import com.Ease.Utils.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.persistence.*;
import java.util.*;

/**
 * Created by thomas on 10/04/2017.
 */
@Entity
@Table(name = "channels")
public class Channel {
    @Id
    @GeneratedValue
    @Column(name = "id")
    protected Integer db_id;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    protected Team team;

    @Column(name = "name")
    protected String name;

    @Column(name = "purpose")
    protected String purpose;

    @ManyToMany
    @JoinTable(name = "channelAndTeamUserMap", joinColumns = {@JoinColumn(name = "channel_id")}, inverseJoinColumns = {@JoinColumn(name = "team_user_id")})
    protected List<TeamUser> teamUsers = new LinkedList<>();

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    protected Set<ChannelNotification> channelNotifications = new HashSet<>();

    public Channel(Team team, String name, String purpose, List<TeamUser> teamUsers) {
        this.team = team;
        this.name = name;
        this.teamUsers = teamUsers;
        this.purpose = purpose;
    }

    public Channel(Team team, String name, String purpose) {
        this.team = team;
        this.name = name;
        this.purpose = purpose;
    }

    public Channel(String name, String purpose) {
        this.name = name;
        this.purpose = purpose;
    }

    public Channel() {
    }

    public Integer getDb_id() {
        return db_id;
    }

    public void setDb_id(Integer db_id) {
        this.db_id = db_id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public List<TeamUser> getTeamUsers() {
        return teamUsers;
    }

    public void setTeamUsers(List<TeamUser> teamUsers) {
        this.teamUsers = teamUsers;
    }

    public Set<ChannelNotification> getChannelNotifications() {
        return channelNotifications;
    }

    public void setChannelNotifications(Set<ChannelNotification> channelNotifications) {
        this.channelNotifications = channelNotifications;
    }

    public void addTeamUser(TeamUser teamUser) {
        if (this.teamUsers.contains(teamUser))
            return;
        this.teamUsers.add(teamUser);
    }

    public void removeTeamUser(TeamUser teamUser, DataBaseConnection db) throws HttpServletException {
        try {
            DatabaseRequest request = db.prepareRequest("DELETE FROM channelAndTeamUserMap WHERE team_user_id = ? AND channel_id = ?;");
            request.setInt(teamUser.getDb_id());
            request.setInt(this.getDb_id());
            request.set();
            this.teamUsers.remove(teamUser);
        } catch (GeneralException e) {
            throw new HttpServletException(HttpStatus.InternError, e);
        }

    }

    public JSONObject getJson() {
        JSONObject res = this.getSimpleJson();
        return res;
    }

    public void edit(JSONObject editJson) {
        String name = (String) editJson.get("name");
        if (name != null)
            this.name = name;
    }

    public JSONObject getSimpleJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", this.getDb_id());
        jsonObject.put("name", this.getName());
        jsonObject.put("purpose", this.getPurpose());
        JSONArray jsonArray = new JSONArray();
        for (TeamUser teamUser : this.getTeamUsers())
            jsonArray.add(teamUser.getDb_id());
        jsonObject.put("userIds", jsonArray);
        return jsonObject;
    }

    public void editName(String name) {
        if (name.equals(this.getName()))
            return;
        this.name = name;
    }

    public void editPurpose(String purpose) {
        if (purpose.equals(this.getPurpose()))
            return;
        this.purpose = purpose;
    }
}
