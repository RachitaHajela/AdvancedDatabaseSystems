package nyu.edu.src.transcation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import nyu.edu.src.store.SiteAccessed;

public class Transaction {

    public enum Status {
        RUNNING, WAITING, ABORTED, COMMITED
    }

    private String ID;
    private int timeStamp;
    private Set<SiteAccessed> sitesAccessed;
    private Status transactionStatus;
    private Boolean isReadOnly;
    private HashMap<String, Integer> snapshotIfReadOnly;
    private HashMap<String, Integer> uncommittedVariables;

    public Transaction(String id, int timeStamp, Boolean isReadOnly) {
        this.ID = id;
        this.timeStamp = timeStamp;
        this.isReadOnly = isReadOnly;
        sitesAccessed = new HashSet<SiteAccessed>();
        uncommittedVariables = new HashMap<String, Integer>();
    }

    public String getID() {
        return ID;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public Status getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(Status transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public Boolean getIsReadOnly() {
        return isReadOnly;
    }

    public HashMap<String, Integer> getSnapshotIfReadOnly() {
        return snapshotIfReadOnly;
    }

    public void setSnapshotIfReadOnly(HashMap<String, Integer> map) {
        this.snapshotIfReadOnly = map;
    }

    public Set<SiteAccessed> getSitesAccessed() {
        return sitesAccessed;
    }

    /**
     * Used to keep track of the sites accessed by a transaction.
     * 
     * @param siteAccessed
     *            - the site that was accessed by the Transaction
     * 
     * @author Rachita & Anto
     */
    public void addToSitesAccessed(SiteAccessed siteAccessed) {
        this.sitesAccessed.add(siteAccessed);
    }

    /**
     * Used to keep track of the variables that are changed but uncommitted.
     * 
     * @param var
     *            - the variable that was changed by the transaction
     * @param value
     *            - the value that we are writing to the variable before the
     *            commit
     * 
     * @author Rachita & Anto
     */
    public void addToUncommitedVariables(String var, int value) {
        this.uncommittedVariables.put(var, value);
    }

    public HashMap<String, Integer> getUncommitedVariables() {
        return this.uncommittedVariables;
    }

    /**
     * take appropriate action on commit
     * 
     * @param timestamp
     *            - the timestamp of the transaction commit
     * 
     * @author Rachita & Anto
     */
    public void commit(int timestamp) {
        this.transactionStatus = Status.COMMITED;
    }

    /**
     * take appropriate action on abort
     * 
     * @param timestamp
     *            - the timestamp of the transaction abort
     * 
     * @author Rachita & Anto
     */
    public void abort(int timestamp) {
        this.transactionStatus = Status.ABORTED;
    }
}
