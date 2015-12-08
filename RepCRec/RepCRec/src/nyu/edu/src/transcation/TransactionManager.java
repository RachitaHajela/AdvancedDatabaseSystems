package nyu.edu.src.transcation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nyu.edu.src.lock.Lock;
import nyu.edu.src.store.DataManager;
import nyu.edu.src.store.Site;
import nyu.edu.src.store.Site.ServerStatus;
import nyu.edu.src.store.SiteAccessed;
import nyu.edu.src.transcation.Transaction.Status;
import nyu.edu.src.transcation.WaitOperation.OPERATION;

public class TransactionManager {

    public static final int numberOfTotalSites = 10;
    public static final int numberOfTotalVariables = 20;
    
    private DataManager dataManager;
    private Map<String, Transaction> transactionsMap;
    private List<WaitOperation> waitingOperations;
    int currentTime = 1;
    
    public TransactionManager() {
        dataManager = new DataManager();
        transactionsMap = new HashMap<String, Transaction>();
        waitingOperations = new ArrayList<WaitOperation>();
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    /**
     * increases the time by one. (equivalent to 1 tick)
     * 
     * @author Rachita & Anto
     */
    public void tick() {
        currentTime++;
        checkWaitingOperations();
    }

    /**
     * transaction makes a commit request
     * 
     * @param transaction
     *            - The transaction that is trying to commit
     * @param timeStamp
     *            - the timestamp of the commit request
     * 
     * @author Rachita & Anto
     */
    public void commitRequest(Transaction transaction, int timestamp) {
        /*System.out.println("COMMIT : timestamp = " + timestamp
                + ", transaction = " + transaction.getID());
*/
        HashMap<String, Integer> uncommitted = transaction
                .getUncommitedVariables();
        for (String variable : uncommitted.keySet()) {
            for (Site s : dataManager.getSites()) {
                if (s.variableExistsOnThisSite(variable)) {
                    if ((s.getStatus().compareTo(ServerStatus.UP) == 0 || s
                            .getStatus().compareTo(ServerStatus.RECOVERING) == 0)
                            && s.isWriteLockTaken(transaction, variable)) {
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
     *            - the timestamp of the beginning of the Transaction
     * @param transaction
     *            - The transaction that is starting
     * 
     * @author Rachita & Anto
     */
    public void begin(int timeStamp, String transactionID) {
        /*System.out.println("BEGIN : timestamp = " + timeStamp
                + ", transaction = " + transactionID);*/
        Transaction trans = new Transaction(transactionID, timeStamp, false);
        transactionsMap.put(transactionID, trans);
    }

    /**
     * transaction begins in Read-Only mode
     * 
     * @param timeStamp
     *            - the timestamp of the beginning of the Transaction in
     *            Read-Only Mode
     * @param transaction
     *            - The transaction that is starting in Read-Only Mode
     * 
     * @author Rachita & Anto
     */
    public void beginRO(int timeStamp, String transactionID) {
        /*System.out.println("BEGINRO : timestamp = " + timeStamp
                + ", transaction = " + transactionID);*/
        Transaction trans = new Transaction(transactionID, timeStamp, true);
        trans.setSnapshotIfReadOnly(takeSnapshot());
        transactionsMap.put(transactionID, trans);
    }

    /**
     * Takes the snapshot to be used by Read-Only Transactions
     * 
     * @author Rachita & Anto
     */

    private HashMap<String, Integer> takeSnapshot() {
        HashMap<String, Integer> snapshot = new HashMap<String, Integer>();

        // adding even variables
        for (int i = 0; i < 10; i = i + 2) {
            if (dataManager.getSites().get(i).getStatus() == ServerStatus.UP) {
                for (int var = 2; var <= 20; var = var + 2) {
                    snapshot.put("x" + var, dataManager.getSites().get(i).read(var));
                }
                break;
            }

        }
        // adding odd variables
        for (int i = 1; i < 10; i = i + 2) {
            if (dataManager.getSites().get(i).getStatus() == ServerStatus.UP) {
                snapshot.put("x" + i, dataManager.getSites().get(i).read(i));
                snapshot.put("x" + (i + 10), dataManager.getSites().get(i).read(i + 10));
            }
        }
        return snapshot;
    }

    /**
     * transaction ends, checks whether it can commit or not
     * 
     * @param timeStamp
     *            - the timestamp of the ending of the Transaction
     * @param transaction
     *            - The transaction that is ending
     * 
     * @author Rachita & Anto
     */
    public void end(int timestamp, String transactionID) {

        Transaction transaction = transactionsMap.get(transactionID);
        if (transaction == null) {
            System.out.println("Incorrect transaction name "
                    + "or transaction has already ended");
            return;
        }

        if (transaction.getTransactionStatus() == Status.ABORTED) {
            System.out.println("Transaction " + transactionID
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
                    System.out.println("Transaction " + transactionID
                            + " aborted because Site "
                            + s.getSiteAccessed().getId() + " was down!");
                    transaction.abort(timestamp);
                    clearLocksAndUnblock(timestamp, transaction);
                    return;
                }
            }
            System.out.println("Transaction " + transactionID + " commits");
            commitRequest(transaction, transactionTimestamp);
            transaction.commit(timestamp);
        }

        else {
            System.out.println("Transaction " + transactionID + " commits");
            transaction.commit(timestamp);
        }
        clearLocksAndUnblock(timestamp, transaction);
    }

    /**
     * 
     * @param timeStamp
     *            - the time moment that the Transaction comes to this method
     * @param transaction
     *            - The transaction that we need to clear from the sites lock
     *            table
     * 
     * @author Rachita & Anto
     */
    public void clearLocksAndUnblock(int timestamp, Transaction transaction) {
        transactionsMap.remove(transaction.getID());

        for (Site s : dataManager.getSites()) {
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

    /**
     * Checks if any of the waiting operations can be started up
     * 
     * @author Rachita & Anto
     */
    public void checkWaitingOperations() {
        int count = waitingOperations.size();

        List<WaitOperation> dummyOperations = new ArrayList<WaitOperation>();

        for (int i = 0; i < waitingOperations.size(); i++) {
            dummyOperations.add(waitingOperations.get(i));
        }
        
        for (int i = 0; i < count; i++) {
            WaitOperation waitTask = dummyOperations.get(i);
            waitingOperations.remove(waitTask);

            if(!transactionsMap.containsValue(waitTask.getWaitingTransaction())) {
            	continue;
            }
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
                    // take lock if not then wait or die
                    if (site.isWriteLockAvailable(transaction, variable)) {
                        site.getWriteLock(transaction, variable);
                        SiteAccessed siteAccessed = new SiteAccessed(site,
                                currentTime);
                        transaction.addToSitesAccessed(siteAccessed);

                        // check if transaction is waiting for more locks

                        for (int j = i; j < count; j++) {
                            if (dummyOperations.get(j).getWaitingTransaction() == transaction) {
                            }
                        }

                        transaction.addToUncommitedVariables(variable,
                                waitTask.getValue());
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
                        System .out.println("Transaction " + transaction.getID()
                                + " Aborted because it was waiting for Older transaction");
                        clearLocksAndUnblock(currentTime, transaction);
                    }

                }

            }

        }
    }

    /**
     * transaction makes a write request
     * 
     * @param timeStamp
     *            - the timestamp of the write request of the Transaction
     * @param transaction
     *            - The Transaction that is making the write request
     * @param variable
     *            - The variable that we are writing
     * @param val
     *            - The value that we writing to the variable
     * 
     * @author Rachita & Anto
     */
    public void writeRequest(int timestamp, String transactionID,
            String variable, String val) {
        int value = Integer.parseInt(val);
        /*System.out.println("WRITE : timestamp = " + timestamp
                + ", transaction = " + transactionID + ", variable = "
                + variable + ", value = " + value);*/

        Transaction transaction = transactionsMap.get(transactionID);
        int varNum = Integer.parseInt(variable.substring(1));

        // if variable is odd
        if (varNum % 2 != 0) {
            int siteNum = varNum % 10;
            Site site = dataManager.getSites().get(siteNum);
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
                } else { // lock not available
                    if (site.transactionWaits(transaction, variable)) {
                        transaction.setTransactionStatus(Status.WAITING);
                        WaitOperation waitOperation = new WaitOperation(
                                transaction, OPERATION.WRITE, variable, site,
                                value);
                        waitingOperations.add(waitOperation);
                        
                    } else {
                        transaction.setTransactionStatus(Status.ABORTED);
                       System .out.println("Transaction " + transactionID
                                + " Aborted because it was waiting for Older transaction");
                        clearLocksAndUnblock(timestamp, transaction);
                        
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

    /**
     * 
     * @param timeStamp
     *            - the timestamp of the write request of the Transaction
     * @param transaction
     *            - The Transaction that is making the write request
     * @param variable
     *            - The variable that we are writing
     * @param value
     *            - The value that we writing to the variable
     * 
     * @author Rachita & Anto
     */
    private void writeRequestEvenVariable(int timestamp,
            Transaction transaction, String variable, int value) {
        boolean allLocksAcquired = true;

        for (int i = 0; i < 10; i++) {
            Site site = dataManager.getSites().get(i);
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
                                + " Aborted because it was waiting for Older transaction");
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
     *            - the timestamp of the read request of the Transaction
     * @param transaction
     *            - The Transaction that is making the read request
     * @param variable
     *            - The variable that we are reading
     * 
     * @author Rachita & Anto
     */
    public void readRequest(int timestamp, String transactionID, String variable) {
        /*System.out.println("READ : timestamp = " + timestamp
                + ", transaction = " + transactionID + ", variable = "
                + variable);*/

        Transaction transaction = transactionsMap.get(transactionID);
        int varNum = Integer.parseInt(variable.substring(1));

        if (transaction.getIsReadOnly()) {
            readOnlyRequest(transaction, variable);
            return;
        }

        // if variable is odd
        if (varNum % 2 != 0) {
            int siteNum = varNum % 10;
            Site site = dataManager.getSites().get(siteNum);
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
                                + " Aborted because it was waiting for Older transaction.");
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
                Site site = dataManager.getSites().get(i);
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
            if (!valueRead) { // either all servers are down or there is a write
                // lock.
                for (int i = 0; i < 10; i++) {
                    if (dataManager.getSites().get(i).getStatus() == ServerStatus.UP) {
                        if (!dataManager.getSites().get(i).transactionWaits(transaction,
                                variable)) {
                            transaction.setTransactionStatus(Status.ABORTED);
                            System .out.println("Transaction " + transactionID
                                    + " Aborted because it was waiting for Older transaction");
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

    /**
     * 
     * @param transaction
     *            - The Transaction that is making the read request
     * @param var
     *            - The variable that we are reading
     * 
     * @author Rachita & Anto
     */
    private void readOnlyRequest(Transaction transaction, String var) {
        HashMap<String, Integer> snapshot = transaction.getSnapshotIfReadOnly();
        if (snapshot.containsKey(var)) {
            System.out.println(transaction.getID() + " reads " + var
                    + " value: " + snapshot.get(var));
        } else {
        	 //value was not present at the time of snapshot
        	//get value now
        	 int varNum = Integer.parseInt(var.substring(1));
        	 int siteNum = varNum % 10;
             Site site = dataManager.getSites().get(siteNum);
             if(site.getStatus() == ServerStatus.UP) {
            	 System.out.println(transaction.getID() + " reads " + var
                         + " value: " + site.read(var));
             }
             else if((site.getStatus() == ServerStatus.RECOVERING) && site.isReadLockAvailable(var)) {
            	 System.out.println(transaction.getID() + " reads " + var
                         + " value: " + site.read(var));
             }
             else {//create wait operation
            	 WaitOperation waitOperation = new WaitOperation(transaction,
                         OPERATION.READ, var);
                 waitingOperations.add(waitOperation);
             }
        }
    }

    /**
     * gives the committed values of all copies of all variables at all sites,
     * sorted per site.
     * 
     * @author Rachita & Anto
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
     *            - identifier of the site that we are dumping from
     * 
     * @author Rachita & Anto
     */
    public void dump(int siteNum) {
        System.out.println("DUMP : siteNum = " + siteNum);
        ServerStatus status = dataManager.getSites().get(siteNum - 1).getStatus();

        if (status == ServerStatus.UP || status == ServerStatus.RECOVERING) {
            System.out.println(dataManager.getSites().get(siteNum - 1).getVariables());
        } else if (status == ServerStatus.DOWN) {
            System.out.println("Site " + dataManager.getSites().get(siteNum - 1).getId()
                    + ": Down");
        }
    }

    /**
     * gives the committed values of all copies of variable var at all sites.
     * 
     * @param var
     *            - the variable that we are dumping from all sites that contain
     *            the variable
     * 
     * @author Rachita & Anto
     */
    public void dump(String var) {
        int variableID = Integer.parseInt(var.substring(1));

        for (Site s : dataManager.getSites()) {
            ServerStatus status = s.getStatus();

            if (status == ServerStatus.UP || status == ServerStatus.RECOVERING) {
                String val = s.getVariableString(variableID);
                if (!val.equalsIgnoreCase("ignore")) {
                    System.out.println("SITE " + s.getId() + " : "
                            + s.getVariableString(variableID));
                }
            } else if (status == ServerStatus.DOWN) {
                System.out.println("Server is Down!");
            }
        }
    }
}
