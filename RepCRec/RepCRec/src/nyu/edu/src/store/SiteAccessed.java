package nyu.edu.src.store;

public class SiteAccessed {

    /**
     * This class is used to keep track of the sites that have been accessed by
     * a Transaction
     * 
     * @author Rachita & Anto
     */

    private Site site;
    private int timeOfAccess;

    public SiteAccessed(Site siteAccessed, int timeOfAccess) {
        super();
        this.site = siteAccessed;
        this.timeOfAccess = timeOfAccess;
    }

    public Site getSiteAccessed() {
        return site;
    }

    public int getTimeOfAccess() {
        return timeOfAccess;
    }
}