package nyu.edu.src.store;

import java.util.ArrayList;
import java.util.List;

import nyu.edu.src.store.Site.ServerStatus;

public class DataManager {

    private List<Site> sites;

    public List<Site> getSites() {
        return sites;
    }

    public DataManager() {
        setUp();
    }

    /**
     * sets up the initial database: sites with variables and their initial
     * values
     * 
     * @author Rachita & Anto
     */
    public void setUp() {
        // creating variables
        List<Variable> varList = new ArrayList<Variable>();
        for (int i = 1; i <= 20; i++) {
            Variable v = new Variable(i, i * 10);
            varList.add(v);
        }
        // creating sites
        sites = new ArrayList<Site>();
        for (int i = 1; i <= 10; i++) {
            Site site = new Site(i);
            site.setStatus(ServerStatus.UP);
            sites.add(site);
        }

        // adding variables to site
        for (int i = 1; i <= 20; i++) {
            // var is odd
            if (i % 2 != 0) {
                sites.get(i % 10).addVariableToSite(varList.get(i - 1));
            } else { // add to all sites
                for (int j = 0; j < 10; j++) {
                    sites.get(j).addVariableToSite(varList.get(i - 1));
                }
            }
        }
    }

    /**
     * site fails
     * 
     * @param timeStamp
     *            - the timestamp of the failure of a site
     * @param siteID
     *            - the identifier of the site that is failing
     * 
     * @author Rachita & Anto
     */
    public void fail(int timestamp, int siteID) {
        Site site = getSites().get(siteID - 1);

        if (site != null) {
            System.out.println("FAIL : timestamp = " + timestamp
                    + ", siteID = " + siteID);
            site.failure(timestamp);
        }
    }

    /**
     * site recovers
     * 
     * @param timeStamp
     *            - the timestamp of the recovery of a site
     * @param siteID
     *            - the identifier of the site that is recovering
     * 
     * @author Rachita & Anto
     */
    public void recover(int siteID) {
        Site site = getSites().get(siteID - 1);
        if (site != null) {
            System.out.println("RECOVER : siteID = " + siteID);
            site.recover();
        }
    }

}
