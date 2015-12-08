package nyu.edu.src.transaction;

import nyu.edu.src.store.Site;

public class WaitOperation {

    /**
     * This class is used to keep track of the sites that have been put on the
     * Wait Queue. And we check if we can start up Operations from this on each
     * time tick
     * 
     * @author Rachita & Anto
     */

    public enum OPERATION {
        READ, WRITE;
    }

    private Transaction waitingTransaction;
    private OPERATION operationType;
    private String variable;
    private Site waitSite;
    private int value;

    public WaitOperation(Transaction t, OPERATION o, String var) {
        this.waitingTransaction = t;
        this.operationType = o;
        this.variable = var;
    }

    public WaitOperation(Transaction t, OPERATION o, String var, Site site,
            int val) {
        this.waitingTransaction = t;
        this.operationType = o;
        this.variable = var;
        this.waitSite = site;
        this.value = val;
    }

    public Transaction getWaitingTransaction() {
        return waitingTransaction;
    }

    public OPERATION getWaitOperation() {
        return operationType;
    }

    public String getVariable() {
        return variable;
    }

    public Site getWaitSite() {
        return waitSite;
    }

    public int getValue() {
        return value;
    }

}
