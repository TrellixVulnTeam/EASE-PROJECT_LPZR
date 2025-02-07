package com.Ease.Context.Catalog;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.json.simple.JSONArray;

import com.Ease.Context.Group.Group;
import com.Ease.Dashboard.User.User;
import com.Ease.Utils.DataBaseConnection;
import com.Ease.Utils.GeneralException;
import com.Ease.Utils.ServletManager;

public class Catalog {
    /*
     *
     * Constructor
     *
     */
    protected Map<Integer, Sso> ssoIdMap = new HashMap<>();
    protected List<Sso> ssos;
    protected Map<Integer, Website> websiteIdMap = new HashMap<>();
    protected List<Website> websites;
    protected Map<Integer, Tag> tagIdMap = new HashMap<>();
    protected List<Tag> tags;

    public static Catalog updateCatalog(DataBaseConnection db, ServletContext context) throws GeneralException {
        Catalog newCatalog = new Catalog(db, context);
        return newCatalog;
    }

    public Catalog(DataBaseConnection db, ServletContext context) throws GeneralException {

        ssos = Sso.loadSsos(db, context);
        for (Sso sso : ssos)
            ssoIdMap.put(sso.getDbid(), sso);
        websites = Website.loadWebsites(db, ssoIdMap, context);
        for (Website site : websites) {
            websiteIdMap.put(site.getDb_id(), site);
        }
        for (Website site : websites)
            site.loadLoginWithWebsites(db, this);
        tags = Tag.loadTags(db, context);
        for (Tag tag : tags) {
            tagIdMap.put(tag.getDbId(), tag);
            tag.setSites(websiteIdMap, db);
        }
    }

    public Website addWebsite(String url, String name, String homePage, String folder, boolean haveLoginButton, boolean noLogin, boolean noScrap, String[] haveLoginWith, String[] infoNames, String[] infoTypes, String[] placeholders, String[] placeholderIcons, Integer ssoId, String team_id, ServletManager sm) throws GeneralException {
        Website site = Website.createWebsite(url, name, homePage, folder, haveLoginButton, noLogin, noScrap, haveLoginWith, infoNames, infoTypes, placeholders, placeholderIcons, this, ssoId, team_id, sm.getServletContext(), sm.getDB());
        websites.add(site);
        websiteIdMap.put(site.getDb_id(), site);
        return site;
    }

	/*
	 * 
	 * Getter And Setter
	 * 
	 */

    public Website getWebsiteWithId(Integer db_id) throws GeneralException {
        Website ret = null;
        ret = this.websiteIdMap.get(db_id);
        if (ret != null)
            return ret;
        throw new GeneralException(ServletManager.Code.InternError, "This website doesn't exist.");
    }

    public List<Website> getWebsites() {
        return this.websites;
    }

    public List<Website> getWebsitesAlphabetically() {
        List<Website> res = new LinkedList<Website>();
        res.addAll(this.websites);
        Collections.sort(res, new Comparator<Website>() {
            public int compare(Website w1, Website w2) {
                return w1.getName().compareToIgnoreCase(w2.getName());
            }
        });
        return res;
    }

    public Website getWebsiteWithName(String name) throws GeneralException {
        for (Website site : this.websites) {
            if (site.getName().equals(name))
                return site;
        }
        throw new GeneralException(ServletManager.Code.ClientError, "This website name dosen't exist.");
    }

    public List<Website> getPublicWebsites() {
        List<Website> res = new LinkedList<Website>();
        this.websites.forEach((website) -> {
            if (website.isInPublicCatalog() && !website.isNew())
                res.add(website);
        });
        res.addAll(getNewWebsites());
        return res;
    }

    public List<Website> getPublicWebsitesForUser(User user) {
        List<Website> res = new LinkedList<Website>();
        List<Website> newWebsites = new LinkedList<Website>();
        this.websites.forEach((website) -> {
            if (website.isInPublicCatalogForUser(user)) {
                if (website.isNew())
                    newWebsites.add(website);
                else
                    res.add(website);
            }
        });
        res.addAll(newWebsites);
        return res;
    }

    public List<Website> getPublicWebsitesForGroups(List<Group> groups) {
        List<Website> res = new LinkedList<Website>();
        boolean containsWebsite = false;
        for (Website website : websites) {
            containsWebsite = false;
            for (Group group : groups) {
                if (group.containsWebsite(website) && !website.isNew()) {
                    res.add(website);
                    containsWebsite = true;
                    break;
                }
            }
            if (containsWebsite)
                continue;
            if (website.isInPublicCatalog() && !website.isNew())
                res.add(website);
        }
        return res;
    }

    public List<Website> getNewWebsitesForGroups(List<Group> groups) {
        List<Website> res = new LinkedList<Website>();
        boolean containsWebsite = false;
        for (Website website : websites) {
            containsWebsite = false;
            for (Group group : groups) {
                if (group.containsWebsite(website) && website.isNew()) {
                    res.add(website);
                    containsWebsite = true;
                    break;
                }
            }
            if (containsWebsite)
                continue;
            if (website.isInPublicCatalog() && website.isNew())
                res.add(website);
        }
        return res;
    }

    public List<Website> getNewWebsites() {
        List<Website> res = new LinkedList<Website>();
        this.websites.forEach((website) -> {
            if (website.isInPublicCatalog() && website.isNew())
                res.add(website);
        });
        return res;
    }

    public List<Tag> getTags() {
        return this.tags;
    }
	
	/*public List<Tag> getTagsAlphabetically() {
		List<Tag> res = this.tags;
		Collections.sort(res, new Comparator<Tag>() {
			public int compare(Tag t1, Tag t2) {
				return t1.getName().compareToIgnoreCase(t2.getName());
			}
		});
		return res;
	}*/

    public JSONArray search(String search, JSONArray tags) throws GeneralException {
        JSONArray result = new JSONArray();
        if (tags.size() <= 0) {
            for (Website site : this.websites) {
                if (site.getName().toUpperCase().startsWith(search.toUpperCase()) && site.work()) {
                    result.add(String.valueOf(site.getDb_id()));
                }
            }
        } else {
            Tag tag;
            for (Object tagName : tags) {
                tag = this.tagIdMap.get(Integer.parseInt((String) tagName));
                if (tag != null) {
                    tag.search(result, search);
                } else {
                    throw new GeneralException(ServletManager.Code.ClientError, "This tag dosen't exist.");
                }
            }
        }
        return result;
    }

    public Website getWebsiteWithHost(String host) {
        for (Website site : websites) {
            if (site.getHostname().equals(host))
                return site;
        }
        return null;
    }

    public boolean haveWebsiteNamed(String websiteName) {
        for (Website site : this.websites) {
            if (site.getName().equals(websiteName))
                return true;
        }
        return false;
    }

    public boolean matchUrl(String url) {
        return this.haveWebsiteWithHostUrl(url) || this.haveWebsiteNamed(url) || this.haveWebsiteWithLoginUrl(url);
    }

    public Website getWebsiteNamed(String websiteName) throws GeneralException {
        for (Website site : this.websites) {
            if (site.getName().toUpperCase().equals(websiteName.toUpperCase()))
                return site;
        }
        throw new GeneralException(ServletManager.Code.ClientError, "We don't have this website");
    }

    public boolean haveWebsiteWithLoginUrl(String url) {
        for (Website site : this.websites) {
            if (site.loginUrlMatch(url))
                return true;
        }
        return false;
    }

    public boolean haveWebsiteWithHostUrl(String url) {
        for (Website site : this.websites) {
            if (site.homepageUrlMatch(url))
                return true;
        }
        return false;
    }

    public Website getWebsiteWithLoginUrl(String url) throws GeneralException {
        for (Website site : this.websites) {
            if (site.loginUrlMatch(url))
                return site;
        }
        throw new GeneralException(ServletManager.Code.UserMiss, "This website is not in the catalog.");
    }

    public JSONArray getWebsitesJsonForUser(User user) {
        JSONArray res = new JSONArray();
        for (Website website : this.getPublicWebsitesForUser(user))
            res.add(website.getJsonForCatalog(user));
        Collections.reverse(res);
        return res;
    }

    public List<Sso> getSsos() {
        return this.ssos;
    }

    public JSONArray getSsoJson() {
        JSONArray res = new JSONArray();
        for (Map.Entry<Integer, Sso> entry : this.ssoIdMap.entrySet()) {
            res.add(entry.getValue().getJson());
        }
        return res;
    }

    public List<Tag> getTagsForWebsiteId(int websiteId) throws GeneralException {
        Website website = this.getWebsiteWithId(websiteId);
        List<Tag> res = new LinkedList<Tag>();
        for (Tag tag : this.tags) {
            if (tag.getWebsites().contains(website))
                res.add(tag);
        }
        return res;
    }

    public boolean isWebsiteTaggedWithSingleId(int websiteId) throws GeneralException {
        Website website = this.getWebsiteWithId(websiteId);
        return this.isWebsiteTagged(website);
    }

    public boolean isWebsiteTagged(Website website) {
        for (Tag tag : this.tags) {
            if (tag.getWebsites().contains(website))
                return true;
        }
        return false;
    }

    public void addWebsiteTag(int websiteId, int tagId, ServletManager sm) throws GeneralException {
        Website website = this.websiteIdMap.get(websiteId);
        Tag tag = this.tagIdMap.get(tagId);
        if (website == null)
            throw new GeneralException(ServletManager.Code.ClientError, "This website does not exist");
        if (tag == null)
            throw new GeneralException(ServletManager.Code.ClientError, "This tag does not exist");
        tag.addWebsite(website, sm);

    }

    public void removeWebsiteTag(int websiteId, int tagId, ServletManager sm) throws GeneralException {
        Website website = this.websiteIdMap.get(websiteId);
        Tag tag = this.tagIdMap.get(tagId);
        if (website == null)
            throw new GeneralException(ServletManager.Code.ClientError, "This website does not exist");
        if (tag == null)
            throw new GeneralException(ServletManager.Code.ClientError, "This tag does not exist");
        tag.removeWebsite(website, sm);

    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
        this.tagIdMap.put(tag.getDbId(), tag);
    }

    public void refresh(ServletManager sm) throws GeneralException {
        for (Website website : this.websites)
            website.refresh(sm);
        for (Tag tag : this.tags)
            tag.refresh(sm);
        for (Sso sso : this.ssos)
            sso.refresh(sm);
        List<Website> newWebsites = Website.loadNewWebsites(ssoIdMap, sm.getDB());
        for (Website website : newWebsites) {
            this.websites.add(website);
            this.websiteIdMap.put(website.getDb_id(), website);
        }
        List<Website> allNewWebsites = this.getNewWebsites();
        List<Website> oldWebsites = new LinkedList<Website>();
        this.websites.forEach((Website website) -> {
            if (!website.isNew())
                oldWebsites.add(website);
        });
        Collections.sort(oldWebsites, new Comparator<Website>() {
            @Override
            public int compare(Website o1, Website o2) {
                if (o1.ratio > o2.ratio)
                    return 1;
                else if (o1.ratio == o2.ratio)
                    return 0;
                else
                    return -1;
            }
        });
        oldWebsites.addAll(allNewWebsites);
        this.websites = oldWebsites;
    }

    public void turnOffWebsite(int single_id, ServletManager sm) throws GeneralException {
        this.getWebsiteWithId(single_id).turnOff(sm);
    }

    public void turnOnWebsite(int single_id, ServletManager sm) throws GeneralException {
        this.getWebsiteWithId(single_id).turnOn(sm);
    }

    public List<Website> getWorkingWebsites() {
        List<Website> res = new LinkedList<Website>();
        this.websites.forEach((website) -> {
            if (website.work())
                res.add(website);
        });
        Collections.sort(res, new Comparator<Website>() {
            public int compare(Website w1, Website w2) {
                return w1.compareToWithVisits(w2);
            }
        });
        return res;
    }

    public List<Website> getBrokenWebsites() {
        List<Website> res = new LinkedList<Website>();
        this.websites.forEach((website) -> {
            if (!website.work())
                res.add(website);
        });
        Collections.sort(res, new Comparator<Website>() {
            public int compare(Website w1, Website w2) {
                return w1.compareToWithVisits(w2);
            }
        });
        return res;
    }

    public Tag getTagWithSingleId(int id) throws GeneralException {
        Tag tag = this.tagIdMap.get(id);
        if (tag == null)
            throw new GeneralException(ServletManager.Code.ClientError, "Wrong single_id");
        return tag;
    }

    public void removeTag(Tag tag, ServletManager sm) throws GeneralException {
        tag.removeFromDb(sm);
        this.tags.remove(tag);
        this.tagIdMap.remove(tag.getDbId());
    }

    public void removeTagWithSinlge(int single_id, ServletManager sm) throws GeneralException {
        Tag tag = this.getTagWithSingleId(single_id);
        this.removeTag(tag, sm);
    }

    public void blacklistWebsite(Website website, ServletManager sm) throws GeneralException {
        website.blacklist(sm);
    }

    public void blacklistWebsiteWithSingleId(int single_id, ServletManager sm) throws GeneralException {
        Website website = this.getWebsiteWithId(single_id);
        this.blacklistWebsite(website, sm);
    }

    private void whitelistWebsite(Website website, ServletManager sm) throws GeneralException {
        website.whitelist(sm);
    }

    public void whitelistWebsiteWithSingleId(int single_id, ServletManager sm) throws GeneralException {
        Website website = this.getWebsiteWithId(single_id);
        this.whitelistWebsite(website, sm);
    }

    public Sso getSsoWithDbId(Integer ssoId) throws GeneralException {
        Sso sso = this.ssoIdMap.get(ssoId);
        if (sso == null)
            throw new GeneralException(ServletManager.Code.ClientError, "Sso is null");
        return sso;
    }
}
