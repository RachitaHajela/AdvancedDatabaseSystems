package nyu.edu.src.transcation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nyu.edu.src.store.Site;
import nyu.edu.src.store.Site.ServerStatus;
import nyu.edu.src.store.Variable;

public class TransactionManager {

    public static final int numberOfTotalSites = 10;
    public static final int numberOfTotalVariables = 20;

    private List<Site> sites;
    private List<Transaction> transactions;
    private List<Transaction> waitingTransactions;
    int currentTime = 1;

    /**
     * sets up the initial database: sites with variables and their initial
     * values
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
     * increases the time by one. (equivalent to 1 tick
     */
    public void tick() {
	currentTime++;

	// to do - function to check on waiting transactions
    }

    /**
     * transaction makes a commit request
     * 
     * @param transaction
     * @param timeStamp
     */
    public void commitRequest(Transaction transaction, int timestamp) {
	System.out.println("COMMIT : timestamp = " + timestamp
		+ ", transaction = " + transaction);
    }

    /**
     * transaction begins
     * 
     * @param timeStamp
     * @param transaction
     */
    public void begin(int timeStamp, String transactionID) {
	System.out.println("BEGIN : timestamp = " + timeStamp
		+ ", transaction = " + transactionID);
	Transaction trans = new Transaction(transactionID, timeStamp, false);
	transactions.add(trans);
    }

    /**
     * transaction begins in Read-Only mode
     * 
     * @param timeStamp
     * @param transaction
     */
    public void beginRO(int timeStamp, String transactionID) {
	System.out.println("BEGINRO : timestamp = " + timeStamp
		+ ", transaction = " + transactionID);
	Transaction trans = new Transaction(transactionID, timeStamp, true);
	trans.setSnapshotIfReadOnly(takeSnapshot());
	transactions.add(trans);
    }

    // making public for testing
    public Map<String, Integer> takeSnapshot() {
	Map<String, Integer> snapshot = new HashMap<String, Integer>();

	// adding even variables
	for (int i = 0; i < 10; i = i + 2) {
	    if (sites.get(i).getStatus() == ServerStatus.UP) {
		for (int var = 2; var <= 20; var = var + 2) {
		    snapshot.put("x" + var, sites.get(i).read(var));
		    /*
		     * System.out .println("x" + var + " " +
		     * sites.get(i).read(var));
		     */
		}
		break;
	    }

	}
	// adding odd variables
	for (int i = 1; i < 10; i = i + 2) {
	    if (sites.get(i).getStatus() == ServerStatus.UP) {
		snapshot.put("x" + i, sites.get(i).read(i));
		snapshot.put("x" + (i + 10), sites.get(i).read(i + 10));
		/*
		 * System.out .println("x" + i + " " + sites.get(i).read(i));
		 * System.out .println("x" + (i+10) + " " +
		 * sites.get(i).read(i+10));
		 */}
	}
	// System.out.println(snapshot.size());
	return snapshot;
    }

    /**
     * transaction ends
     * 
     * @param timestamp
     * @param transaction
     */
    public void end(int timestamp, String transaction) {
	System.out.println("END : timestamp = " + timestamp
		+ ", transaction = " + transaction);
    }

    /**
     * site fails
     * 
     * @param timeStamp
     * @param siteID
     */
    public void fail(int timestamp, int siteID) {
        Site site = sites.get(siteID);
        
        if( site != null) {
            System.out.println("FAIL : timestamp = " + timestamp + ", siteID = "
                    + siteID);
            site.failure(timestamp);
        }
    }

    /**
     * site recovers
     * 
     * @param timeStamp
     * @param siteID
     */
    public void recover(int siteID) {
        Site site = sites.get(siteID);
        if( site != null) {
            System.out.println("RECOVER : siteID = " + siteID);
            site.recover();
        }
    }

    /**
     * transaction makes a write request
     * 
     * @param timeStamp
     * @param transaction
     * @param variable
     * @param val
     */
    public void writeRequest(int timestamp, String transaction,
	    String variable, String val) {
	int value = Integer.parseInt(val);
	System.out.println("WRITE : timestamp = " + timestamp
		+ ", transaction = " + transaction + ", variable = " + variable
		+ ", value = " + value);
    }

    /**
     * transaction makes a read request
     * 
     * @param timeStamp
     * @param transaction
     * @param variable
     */
    public void readRequest(int timestamp, String transaction, String variable) {
	System.out
		.println("READ : timestamp = " + timestamp + ", transaction = "
			+ transaction + ", variable = " + variable);
    }

    /**
     * gives the committed values of all copies of all variables at all sites,
     * sorted per site.
     */
    public void dump() {
	System.out.println("DUMP :");
	for (int i = 0; i < 10; i++) {
	    System.out.print("SITE " + (i + 1) + " : ");
	    if (sites.get(i).getStatus() == ServerStatus.UP) {
		System.out.println(sites.get(i).getVariables());
	    } else if (sites.get(i).getStatus() == ServerStatus.DOWN) {
		System.out.println("Server is down!");
	    } else {
		// To DO serverstatus = Recovering
	    }
	}
    }

    /**
     * gives the committed values of all copies of all variables at site siteNUm
     * 
     * @param siteNum
     */
    public void dump(int siteNum) {
	System.out.println("DUMP : siteNum = " + siteNum);
	if (sites.get(siteNum - 1).getStatus() == ServerStatus.UP) {
	    System.out.println(sites.get(siteNum - 1).getVariables());
	} else if (sites.get(siteNum - 1).getStatus() == ServerStatus.DOWN) {
	    System.out.println("Server is Down!");
	} else {
	    // TODO serverstatus = Recovering
	}
    }

    /**
     * gives the committed values of all copies of variable var at all sites.
     * 
     * @param var
     */
    public void dump(String var) {
	int variableID = Integer.parseInt(var.substring(1));

	for (Site s : sites) {
	    if (s.getStatus() == ServerStatus.UP) {
		String val = s.getVariable(variableID);
		if (!val.equalsIgnoreCase("ignore")) {
		    System.out.println("SITE " + s.getId() + " : "
			    + s.getVariable(variableID));
		}
	    } else if (s.getStatus() == ServerStatus.DOWN) {
		System.out.println("Server is Down!");
	    } else {
		// TODO serverstatus = Recovering
	    }
	}
    }
}