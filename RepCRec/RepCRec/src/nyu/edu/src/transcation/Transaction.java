package nyu.edu.src.transcation;

import java.util.HashMap;
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

    public HashMap<String, Integer> getSnapshotIfReadOnly() {
        return (HashMap<String, Integer>) snapshotIfReadOnly;
    }

    public void setSnapshotIfReadOnly(Map<String, Integer> map) {
	this.snapshotIfReadOnly = map;
    }

    public Set<Site> getSitesAccessed() {
		return sitesAccessed;
	}

	public void addToSitesAccessed(Site siteAccessed) {
		this.sitesAccessed.add(siteAccessed);
	}

	/**
     * take appropriate action on commit
	 * @return 
     */
    public void commit(int timestamp) {
    	this.transactionStatus = Status.COMMITED;
    }

    /**
     * take appropriate action on abort
     * @param timestamp2 
     */
    public void abort(int timestamp) {
    	this.transactionStatus = Status.ABORTED;
    }
}
