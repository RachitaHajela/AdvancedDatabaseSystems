package nyu.edu.src.transcation;

public class WaitOperation {

	public enum OPERATION {
		READ, WRITE;
	}

	private Transaction waitingTransaction;
	private OPERATION waitOperation;
	private String variable;

	public WaitOperation(Transaction t, OPERATION o, String var) {
		this.waitingTransaction = t;
		this.waitOperation = o;
		this.variable = var;
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
