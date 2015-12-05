package nyu.edu.src.transcation;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nyu.edu.src.store.Site;
import nyu.edu.src.store.Variable;

public class Transaction {

    public enum Status {
	RUNNING, WAITING, ABORTED, COMMITED
    }

    private String ID;
    private int timeStamp;
    private Set<Site> sitesAccessed;
    private Status transactionStatus;
    private Boolean isReadOnly;
    private Map<String, Integer> snapshotIfReadOnly;

    public Transaction(String id, int timeStamp, Boolean isReadOnly) {
	this.ID = id;
	this.timeStamp = timeStamp;
	this.isReadOnly = isReadOnly;
	sitesAccessed = new HashSet<Site>();
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

    public Map<String, Integer> getSnapshotIfReadOnly() {
	return snapshotIfReadOnly;
    }

    public void setSnapshotIfReadOnly(Map<String, Integer> map) {
	this.snapshotIfReadOnly = map;
    }

    /**
     * take appropriate action on commit
     */
    public void commit() {

    }

    /**
     * take appropriate action on abort
     */
    public void abort() {
    }
}
