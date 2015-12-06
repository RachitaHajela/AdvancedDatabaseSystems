package nyu.edu.src.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import nyu.edu.src.lock.Lock;
import nyu.edu.src.lock.Lock.LockType;
import nyu.edu.src.transcation.Transaction;
import nyu.edu.src.transcation.TransactionManager;

public class Site {

	private int id;
	private Map<Integer, Variable> variables;
	private Map<Integer, Variable> uncommittedVariables;
	// private LockTable lockTable;
	private Map<String, ArrayList<Lock>> lockTable;

	public enum ServerStatus {
		UP, DOWN, RECOVERING;
	}

	private ServerStatus status;

	public Site(int id) {
		this.id = id;
		variables = new HashMap<Integer, Variable>();
		uncommittedVariables = new HashMap<Integer, Variable>();
		// lockTable = new LockTable();
		status = ServerStatus.UP;
	}

	public int getId() {
		return id;
	}

	public ServerStatus getStatus() {
		return status;
	}

	public void setStatus(ServerStatus status) {
		this.status = status;
	}

	public void addVariableToSite(Variable variable) {
		variables.put(variable.getId(), variable);
	}

	public void addVariableToSite(int id, int value) {
		variables.put(id, new Variable(id, value));
	}

	public boolean variableExistsOnThisSite(String id) {
		return variables.containsKey(id);
	}

	public void failure() {
		status = ServerStatus.DOWN;
	}

	public void recover() {
		status = ServerStatus.UP;
	}

	public String getVariables() {

		StringBuilder str = new StringBuilder();
		for (int i = 1; i <= TransactionManager.numberOfTotalVariables; i++) {
			if (variables.containsKey(i)) {
				str.append("x" + i + ":" + variables.get(i).getValue() + ",");
			}
		}

		str.replace(str.lastIndexOf(","), str.length(), "");

		return str.toString();
	}

	public String getVariable(int variableID) {
		if (!variables.containsKey(variableID)) {
			return "ignore";
		}

		return "x" + variableID + ":" + variables.get(variableID).getValue()
				+ " ";
	}

	/*
	 * public Lock isItLocked(String id) { return
	 * isItLocked(getWithoutStartingX(id)); }
	 */

	/*
	 * public Lock isItLocked(int id) {
	 * if(lockTable.getLockTable().containsKey(id)) { return
	 * lockTable.getLockTable().get(id); }
	 * 
	 * return null; }
	 */

	public int read(String id) {
		return read(getWithoutStartingX(id));
	}

	public int read(int id) {
		return variables.get(id).getValue();
	}

	public void write(String id, int value) {
		write(getWithoutStartingX(id), value);
	}

	public void write(int id, int value) {
		uncommittedVariables.put(id, new Variable(id, value));
	}

	public void rollback(String id) {
		rollback(getWithoutStartingX(id));
	}

	public void rollback(int id) {
		uncommittedVariables.remove(id);
	}

	public void commit(String id) {
		commit(getWithoutStartingX(id));
	}

	public void commit(int id) {
		Variable variable = uncommittedVariables.get(id);
		variables.put(id, variable);
		uncommittedVariables.remove(id);
	}

	public boolean isVariableUncommitted(int id) {
		return uncommittedVariables.containsKey(id);
	}

	public int getWithoutStartingX(String id) {
		if (id.startsWith("x")) {
			int idWithoutX = Integer.parseInt(id.substring(1));
			return idWithoutX;
		}

		// ID was not properly provided (not in the form x1, x2, ..., x20)
		return -1;
	}

	public boolean isReadLockAvailable(String variable) {
		if (!lockTable.containsKey(variable)) {
			return true;
		} else {
			ArrayList<Lock> locks = lockTable.get(variable);
			if (locks.get(0).getType() == LockType.READ) { // if the locks are
															// read locks give
															// it
				return true;
			} else {
				return false;
			}
		}
	}

	public void getReadLock(Transaction trans, String variable) {
		if (lockTable.containsKey(variable)) {
			Lock lock = new Lock(trans, LockType.READ);
			ArrayList<Lock> locks = lockTable.get(variable);
			locks.add(lock);
			lockTable.put(variable, locks);
		} else {
			Lock lock = new Lock(trans, LockType.READ);
			ArrayList<Lock> locks = new ArrayList<Lock>();
			locks.add(lock);
			lockTable.put(variable, locks);
		}
	}

	public boolean transactionWaits(Transaction transaction, String variable) {
		
		return false;
	}

}
