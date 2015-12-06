package nyu.edu.src.transcation;

import nyu.edu.src.store.Site;

public class WaitOperation {

	public enum OPERATION {
		READ, WRITE;
	}

	private Transaction waitingTransaction;
	private OPERATION waitOperation;
	private String variable;
	private Site waitSite;
	
	public WaitOperation(Transaction t, OPERATION o, String var) {
		this.waitingTransaction = t;
		this.waitOperation = o;
		this.variable = var;
	}
	
	public WaitOperation(Transaction t, OPERATION o, String var,Site site) {
		this.waitingTransaction = t;
		this.waitOperation = o;
		this.variable = var;
		this.waitSite = site;
	}

	public Transaction getWaitingTransaction() {
		return waitingTransaction;
	}

	public OPERATION getWaitOperation() {
		return waitOperation;
	}

	public String getVariable() {
		return variable;
	}

}
