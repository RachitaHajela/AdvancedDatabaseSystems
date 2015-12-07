package nyu.edu.src.transcation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nyu.edu.src.lock.Lock;
import nyu.edu.src.store.Site;
import nyu.edu.src.store.Site.ServerStatus;
import nyu.edu.src.store.SiteAccessed;
import nyu.edu.src.store.Variable;
import nyu.edu.src.transcation.Transaction.Status;
import nyu.edu.src.transcation.WaitOperation.OPERATION;

public class TransactionManager {

    public static final int numberOfTotalSites = 10;
    public static final int numberOfTotalVariables = 20;

    private List<Site> sites;
    private Map<String, Transaction> transactionsMap;
    private List<WaitOperation> waitingOperations;
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
        transactionsMap = new HashMap<String, Transaction>();
        waitingOperations = new ArrayList<WaitOperation>();
    }

    /**
     * increases the time by one. (equivalent to 1 tick
     */
    public void tick() {
        currentTime++;
        checkWaitingOperations();
    }

    /**
     * transaction makes a commit request
     * 
     * @param transaction
     * @param timeStamp
     */
    public void commitRequest(Transaction transaction, int timestamp) {
        System.out.println("COMMIT : timestamp = " + timestamp
                + ", transaction = " + transaction.getID());

        HashMap<String, Integer> uncommitted = transaction
                .getUncommitedVariables();
        for (String variable : uncommitted.keySet()) {
            for (Site s : sites) {
                if (s.variableExistsOnThisSite(variable)) {
                    if ((s.getStatus().compareTo(ServerStatus.UP) == 0
                            || s.getStatus().compareTo(ServerStatus.RECOVERING) == 0) && s.isWriteLockTaken(transaction,variable)) {
                        s.write(variable, uncommitted.get(variable));
                    }
                }
            }
        }

        transaction.getUncommitedVariables().clear();
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
        transactionsMap.put(transactionID, trans);
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
        transactionsMap.put(transactionID, trans);
    }

    // making public for testing
    public HashMap<String, Integer> takeSnapshot() {
        HashMap<String, Integer> snapshot = new HashMap<String, Integer>();

        // adding even variables
        for (int i = 0; i < 10; i = i + 2) {
            if (sites.get(i).getStatus() == ServerStatus.UP) {
                for (int var = 2; var <= 20; var = var + 2) {
                    snapshot.put("x" + var, sites.get(i).read(var));
                }
                break;
            }

        }
        // adding odd variables
        for (int i = 1; i < 10; i = i + 2) {
            if (sites.get(i).getStatus() == ServerStatus.UP) {
                snapshot.put("x" + i, sites.get(i).read(i));
                snapshot.put("x" + (i + 10), sites.get(i).read(i + 10));
            }
        }
        return snapshot;
    }

    /**
     * transaction ends
     * 
     * @param timestamp
     * @param transaction
     */
    public void end(int timestamp, String transactionID) {

        Transaction transaction = transactionsMap.get(transactionID);
        if (transaction == null) {
            System.out.println("Incorrect transaction name "
                    + "or transaction has already ended");
            return;
        }

        if (transaction.getTransactionStatus() == Status.ABORTED) {
            System.out.println("Transcation " + transactionID
                    + " already aborted");
            return;
        }

        Set<SiteAccessed> setOfSitesAccessed = transaction.getSitesAccessed();

        if (!transaction.getIsReadOnly()) {
            int transactionTimestamp = transaction.getTimeStamp();
            for (SiteAccessed s : setOfSitesAccessed) {
                if (s.getTimeOfAccess() <= s.getSiteAccessed()
                        .getPreviousFailtime()
                        || s.getSiteAccessed().getStatus()
                                .compareTo(ServerStatus.DOWN) == 0) {
                    System.out.println("Transcation " + transactionID
                            + " aborted because Site "
                            + s.getSiteAccessed().getId() + " was down!");
                    transaction.abort(timestamp);
                    clearLocksAndUnblock(timestamp, transaction);
                    return;
                }
            }
            System.out.println("Transcation " + transactionID + " commits");
            commitRequest(transaction, transactionTimestamp);
            transaction.commit(timestamp);
        }

        else {
            System.out.println("Transcation " + transactionID + " commits");
            transaction.commit(timestamp);
        }
        clearLocksAndUnblock(timestamp, transaction);
    }

    public void clearLocksAndUnblock(int timestamp, Transaction transaction) {
        transactionsMap.remove(transaction.getID());

        for (Site s : sites) {
            Map<String, ArrayList<Lock>> siteLockTable = s.getLockTable();
            ArrayList<String> dummySiteLockTable = new ArrayList<String>();
            for (String variable : siteLockTable.keySet()) {
                ArrayList<Lock> lockArrayList = siteLockTable.get(variable);
                ArrayList<Lock> dummyLockArrayList = new ArrayList<Lock>();
                for (Lock lock : lockArrayList) {
                    if (lock.getTransaction().equals(transaction)) {
                        dummyLockArrayList.add(lock);
                    }
                }
                for (Lock lock : dummyLockArrayList) {
                    siteLockTable.get(variable).remove(lock);
                }

                if (siteLockTable.get(variable).size() == 0) {
                    dummySiteLockTable.add(variable);
                }
            }

            for (String variable : dummySiteLockTable) {
                siteLockTable.remove(variable);
            }
        }

        checkWaitingOperations();
    }

    public void checkWaitingOperations() {
        int count = waitingOperations.size();

        List<WaitOperation> dummyOperations = new ArrayList<WaitOperation>();

        for (int i = 0; i < waitingOperations.size(); i++) {
            dummyOperations.add(waitingOperations.get(i));
        }

        for (int i = 0; i < count; i++) {
            WaitOperation waitTask = dummyOperations.get(i);
            waitingOperations.remove(waitTask);

            // Add a comment to this line
            if (waitTask.getWaitOperation() == OPERATION.READ) {
                readRequest(waitTask.getWaitingTransaction().getTimeStamp(),
                        waitTask.getWaitingTransaction().getID(),
                        waitTask.getVariable());
            } else {
                // check if write lock is available on the site:
                Site site = waitTask.getWaitSite();
                Transaction transaction = waitTask.getWaitingTransaction();
                String variable = waitTask.getVariable();
                if (site.getStatus() == ServerStatus.UP
                        || site.getStatus() == ServerStatus.RECOVERING) {
                    boolean moreLocksRequired = false;

                    // take lock if not then wait or die
                    if (site.isWriteLockAvailable(transaction, variable)) {
                        site.getWriteLock(transaction, variable);
                        SiteAccessed siteAccessed = new SiteAccessed(site,
                                currentTime);
                        transaction.addToSitesAccessed(siteAccessed);

                        // check if transaction is waiting for more locks

                        for (int j = i; j < count; j++) {
                            if (dummyOperations.get(j).getWaitingTransaction() == transaction) {
                                moreLocksRequired = true;
                            }
                        }

                        transaction.addToUncommitedVariables(variable, waitTask.getValue());
                    }

                } else { // lock not available
                    if (site.transactionWaits(transaction, variable)) {
                        transaction.setTransactionStatus(Status.WAITING);
                        WaitOperation waitOperation = new WaitOperation(
                                transaction, OPERATION.WRITE, variable, site,
                                waitTask.getValue());
                        waitingOperations.add(waitOperation);
                        
                    } else {
                        transaction.setTransactionStatus(Status.ABORTED);
                        clearLocksAndUnblock(currentTime, transaction);
                    }

                }

            }

        }
    }

    /**
     * site fails
     * 
     * @param timeStamp
     * @param siteID
     */
    public void fail(int timestamp, int siteID) {
        Site site = sites.get(siteID - 1);

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
     * @param siteID
     */
    public void recover(int siteID) {
        Site site = sites.get(siteID - 1);
        if (site != null) {
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
    public void writeRequest(int timestamp, String transactionID,
            String variable, String val) {
        int value = Integer.parseInt(val);
        System.out.println("WRITE : timestamp = " + timestamp
                + ", transaction = " + transactionID + ", variable = "
                + variable + ", value = " + value);

        Transaction transaction = transactionsMap.get(transactionID);
        int varNum = Integer.parseInt(variable.substring(1));

        // if variable is odd
        if (varNum % 2 != 0) {
            int siteNum = varNum % 10;
            Site site = sites.get(siteNum);
            if (site.getStatus() == ServerStatus.UP
                    || site.getStatus() == ServerStatus.RECOVERING) {
                // take lock if not then wait or die
                if (site.isWriteLockAvailable(transaction, variable)) {
                    site.getWriteLock(transaction, variable);
                    transaction.setTransactionStatus(Status.RUNNING);
                    SiteAccessed siteAccessed = new SiteAccessed(site,
                            currentTime);
                    transaction.addToSitesAccessed(siteAccessed);
                    transaction.addToUncommitedVariables(variable, value);
                    System.out.println("lock taken");
                } else { // lock not available
                    if (site.transactionWaits(transaction, variable)) {
                        transaction.setTransactionStatus(Status.WAITING);
                        WaitOperation waitOperation = new WaitOperation(
                                transaction, OPERATION.WRITE, variable, site,
                                value);
                        waitingOperations.add(waitOperation);
                        System.out.println("waiting");
                    } else {
                        transaction.setTransactionStatus(Status.ABORTED);
                        System.out.println("Transaction " + transactionID
                                + " Aborted!");
                        clearLocksAndUnblock(timestamp, transaction);
                        System.out.println("aborted");
                    }

                }

            } else { // the site is down. It waits
                transaction.setTransactionStatus(Status.WAITING);
                WaitOperation waitOperation = new WaitOperation(transaction,
                        OPERATION.WRITE, variable, site, value);
                waitingOperations.add(waitOperation);
            }
        }
        // variable is even
        else {
            writeRequestEvenVariable(timestamp, transaction, variable, value);
        }
    }

    private void writeRequestEvenVariable(int timestamp,
            Transaction transaction, String variable, int value) {
        boolean allLocksAcquired = true;

        for (int i = 0; i < 10; i++) {
            Site site = sites.get(i);
            if (site.getStatus() == ServerStatus.UP
                    || site.getStatus() == ServerStatus.RECOVERING) {
                // if lock can be taken
                if (site.isWriteLockAvailable(transaction, variable)) {
                    site.getWriteLock(transaction, variable);
                    SiteAccessed siteAccessed = new SiteAccessed(site,
                            currentTime);
                    transaction.addToSitesAccessed(siteAccessed);
                } else { // either the transaction waits or gets aborted
                    if (site.transactionWaits(transaction, variable)) {
                        transaction.setTransactionStatus(Status.WAITING);
                        WaitOperation waitOperation = new WaitOperation(
                                transaction, OPERATION.WRITE, variable, site,
                                value);
                        waitingOperations.add(waitOperation);
                        allLocksAcquired = false;
                    } else {
                        transaction.setTransactionStatus(Status.ABORTED);
                        System.out.println("Transaction " + transaction.getID()
                                + " Aborted!");
                        clearLocksAndUnblock(timestamp, transaction);
                        return;
                    }
                }
            }
        }

        if (allLocksAcquired) {
            transaction.addToUncommitedVariables(variable, value);
        }
    }

    /**
     * transaction makes a read request
     * 
     * @param timeStamp
     * @param transaction
     * @param variable
     */
    public void readRequest(int timestamp, String transactionID, String variable) {
        System.out.println("READ : timestamp = " + timestamp
                + ", transaction = " + transactionID + ", variable = "
                + variable);

        Transaction transaction = transactionsMap.get(transactionID);
        int varNum = Integer.parseInt(variable.substring(1));

        if (transaction.getIsReadOnly()) {
            readOnlyRequest(transaction, variable);
            return;
        }

        // if variable is odd
        if (varNum % 2 != 0) {
            int siteNum = varNum % 10;
            Site site = sites.get(siteNum);
            if (site.getStatus() == ServerStatus.UP
                    || site.getStatus() == ServerStatus.RECOVERING) {

                if (site.isReadLockAvailable(variable)) {
                    site.getReadLock(transaction, variable);
                    System.out.println(transactionID + " reads " + variable
                            + " value: " + site.read(variable));
                    transaction.setTransactionStatus(Status.RUNNING);
                    SiteAccessed siteAccessed = new SiteAccessed(site,
                            currentTime);
                    transaction.addToSitesAccessed(siteAccessed);
                } else {
                    if (site.transactionWaits(transaction, variable)) {
                        transaction.setTransactionStatus(Status.WAITING);
                        WaitOperation waitOperation = new WaitOperation(
                                transaction, OPERATION.READ, variable);
                        waitingOperations.add(waitOperation);
                    } else {
                        transaction.setTransactionStatus(Status.ABORTED);
                        System.out.println("Transaction " + transactionID
                                + " Aborted!");
                        clearLocksAndUnblock(timestamp, transaction);
                        return;
                    }
                }

            } else {
                transaction.setTransactionStatus(Status.WAITING);
                WaitOperation waitOperation = new WaitOperation(transaction,
                        OPERATION.READ, variable);
                waitingOperations.add(waitOperation);

            }
        } else // variable is even
        {
            Boolean valueRead = false;
            for (int i = 0; i < 10; i++) {
                Site site = sites.get(i);
                if (site.getStatus() == ServerStatus.UP) {
                    if (site.isReadLockAvailable(variable)) {
                        site.getReadLock(transaction, variable);
                        System.out.println(transactionID + " reads " + variable
                                + " value: " + site.read(variable));
                        valueRead = true;
                        SiteAccessed siteAccessed = new SiteAccessed(site,
                                currentTime);
                        transaction.addToSitesAccessed(siteAccessed);
                        break;
                    }
                }
            }
            if (!valueRead) { //either all servers are down or there is a write lock.
                for(int i=0;i<10;i++) {
                    if(sites.get(i).getStatus() == ServerStatus.UP) {
                        if(!sites.get(i).transactionWaits(transaction, variable)) {
                            transaction.setTransactionStatus(Status.ABORTED);
                            System.out.println("Transaction " + transactionID
                                    + " Aborted!");
                            clearLocksAndUnblock(timestamp, transaction);
                            return;
                        }
                        break;
                    }
                }
                WaitOperation waitOperation = new WaitOperation(transaction,
                        OPERATION.READ, variable);
                waitingOperations.add(waitOperation);
            }

        }

    }

    private void readOnlyRequest(Transaction transaction, String var) {
        HashMap<String, Integer> snapshot = transaction.getSnapshotIfReadOnly();
        if (snapshot.containsKey(var)) {
            System.out.println(transaction.getID() + " reads " + var
                    + " value: " + snapshot.get(var));
        } else {
            // what to do if server was down at the time transaction began?
        }
    }

    /**
     * gives the committed values of all copies of all variables at all sites,
     * sorted per site.
     */
    public void dump() {
        System.out.println("DUMP ALL:");
        for (int i = 1; i <= 10; i++) {
            dump(i);
        }
    }

    /**
     * gives the committed values of all copies of all variables at site siteNUm
     * 
     * @param siteNum
     */
    public void dump(int siteNum) {
        System.out.println("DUMP : siteNum = " + siteNum);
        ServerStatus status = sites.get(siteNum - 1).getStatus();
        
        if (status == ServerStatus.UP || status == ServerStatus.RECOVERING) {
            System.out.println("Site " + sites.get(siteNum - 1).getId() + ": "
                    + sites.get(siteNum - 1).getVariables());
        } else if (status == ServerStatus.DOWN) {
            System.out.println("Site " + sites.get(siteNum - 1).getId()
                    + ": Down");
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
            ServerStatus status = s.getStatus();
            
            if (status == ServerStatus.UP || status == ServerStatus.RECOVERING) {
                String val = s.getVariable(variableID);
                if (!val.equalsIgnoreCase("ignore")) {
                    System.out.println("SITE " + s.getId() + " : "
                            + s.getVariable(variableID));
                }
            } else if (status == ServerStatus.DOWN) {
                System.out.println("Server is Down!");
            }
        }
    }
}
