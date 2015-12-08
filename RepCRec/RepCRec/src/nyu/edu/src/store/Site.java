package nyu.edu.src.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import nyu.edu.src.lock.Lock;
import nyu.edu.src.lock.Lock.LockType;
import nyu.edu.src.transaction.Transaction;
import nyu.edu.src.transaction.TransactionManager;

/**
 * This class represents the Server (or site) where the data is being stored
 * 
 * @author Rachita & Anto
 *
 */
public class Site {

    private int id;
    private HashMap<Integer, Variable> variables;
    private HashMap<String, ArrayList<Lock>> lockTable;
    private HashSet<String> variableHasRecovered;
    private int previousFailtime;

    public enum ServerStatus {
        UP, DOWN, RECOVERING;
    }

    private ServerStatus status;

    public Site(int id) {
        this.id = id;
        variables = new HashMap<Integer, Variable>();
        lockTable = new HashMap<String, ArrayList<Lock>>();
        variableHasRecovered = new HashSet<String>();
        if (id % 2 == 0) {
            variableHasRecovered.add("x" + (id - 1));
            variableHasRecovered.add("x" + (id - 1 + 10));
        }
        status = ServerStatus.UP;
    }

    public HashMap<String, ArrayList<Lock>> getLockTable() {
        return this.lockTable;
    }

    public int getPreviousFailtime() {
        return this.previousFailtime;
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

    /**
     * adds a particular variable to the server
     * @param variable -variable to be added
     * 
     * @author Rachita & Anto
     */
    public void addVariableToSite(Variable variable) {
        variables.put(variable.getId(), variable);
    }

    /**
     * Creates a new variable from id and value and adds it to the site
     * 
     * @param id - id of the variable
     * @param value - value of the variable
     * 
     * @author Rachita & Anto
     */
    public void addVariableToSite(int id, int value) {
        variables.put(id, new Variable(id, value));
    }

    /**
     * returns true if the variable with the given id is present on the site
     * 
     * @param id id of the variable
     * @return true or false
     * 
     * @author Rachita & Anto
     */
    public boolean variableExistsOnThisSite(String id) {
        return variables.containsKey(getWithoutStartingX(id));
    }

    /**
     * handles the failure of the site.
     * 
     * @param timestamp - the time at which failure happened
     * 
     * @author Rachita & Anto
     */
    public void failure(int timestamp) {
        status = ServerStatus.DOWN;
        lockTable.clear();
        variableHasRecovered.clear();
        previousFailtime = timestamp;
    }

    /**
     * handles recovery of the site
     * 
     * @author Rachita & Anto
     */
    public void recover() {
        int id = this.getId();
        if (id % 2 == 0) {
            variableHasRecovered.add("x" + (id - 1));
            variableHasRecovered.add("x" + (id - 1 + 10));
        }
        status = ServerStatus.RECOVERING;

    }

    /**
     * returns all the variables and their value at the site
     * 
     * @return String containing all the data
     * 
     * @author Rachita & Anto
     * 
     */
    public String getVariables() {

        StringBuilder str = new StringBuilder();
        for (int i = 1; i <= TransactionManager.numberOfTotalVariables; i++) {
            if (variables.containsKey(i)) {
                str.append("x" + i + ":" + variables.get(i).getValue() + ", ");
            }
        }

        str.replace(str.lastIndexOf(","), str.length(), "");

        return str.toString();
    }

    public String getVariableString(int variableID) {
        if (!variables.containsKey(variableID)) {
            return "ignore";
        }

        return "x" + variableID + ":" + variables.get(variableID).getValue()
                + " ";
    }

    /**
     * returns the current value of the variable.
     * 
     * @param id -id of the variable
     * @return current value
     * 
     * @author Rachita & Anto
     */
    public int read(String id) {
        return read(getWithoutStartingX(id));
    }

    /**
     * returns the current value of the variable.
     * 
     * @param id variable num
     * 
     * @return variable's value
     * 
     * @author Rachita & Anto
     */
    public int read(int id) {
        return variables.get(id).getValue();
    }

    /**
     * writes the value of the variable to the server
     * 
     * @param id - variable id
     * @param value - value to be committed
     * 
     * @author Rachita & Anto
     */
    public void write(String id, int value) {
    	if (status.equals(ServerStatus.RECOVERING)) {
            variableHasRecovered.add(id);
            if (variableHasRecovered.size() == variables.size()) {
                setStatus(ServerStatus.UP);
            }
        }
        addVariableToSite(getWithoutStartingX(id), value);
    }

    private int getWithoutStartingX(String id) {
        if (id.startsWith("x")) {
            int idWithoutX = Integer.parseInt(id.substring(1));
            return idWithoutX;
        }
        // ID was not properly provided (not in the form x1, x2, ..., x20)
        return -1;
    }

    /**
     * checks if a transaction should wait for the other transaction to finish or it should abort
     * 
     * @param transaction -the transaction
     * @param variable -the variable on which it wants lock
     * @return -true or false
     * 
     * @author Rachita & Anto
     * 
     */
    public boolean transactionWaits(Transaction transaction, String variable) {
        if(!(this.status == ServerStatus.DOWN)) {
            ArrayList<Lock> locks = lockTable.get(variable);
            Transaction transHoldingLock = locks.get(0).getTransaction();
            if (transHoldingLock.getTimeStamp() < transaction.getTimeStamp()) {
            	 System .out.println("Transaction " + transaction.getID()
                         + " Aborted because " + transHoldingLock.getID()+" has  lock on "+variable);
                return false;
            }
        }
        return true;
    }

    /**
     * checks if read lock can be acquired on the variable
     * 
     * @param variable -the variable on which read lock is required
     * @return - true or false
     * 
     * @author Rachita & Anto
     */
    public boolean isReadLockAvailable(String variable) {
        if (status.equals(ServerStatus.RECOVERING)
                && !variableHasRecovered.contains(variable)) {
            return false;
        }
        if (!lockTable.containsKey(variable)) {
            return true;
        } else {
            ArrayList<Lock> locks = lockTable.get(variable);
            if (locks.get(0).getType() == LockType.READ) { // if the locks are
                                                           // read locks give it
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * the transaction takes the read lock on the variable
     * 
     * @param trans -the transaction
     * @param variable -the variable on which read lock is taken
     * 
     * @author Rachita & Anto
     */
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

    /**
     * checks if a write lock can be taken by the transaction on the variable
     * 
     * @param transaction - the transaction
     * @param variable  -the variable
     * @return  true or false
     * 
     * @author Rachita & Anto
     */
    public boolean isWriteLockAvailable(Transaction transaction, String variable) {
        if (!lockTable.containsKey(variable)) {
            return true;
        }
        if (lockTable.get(variable).size() == 1) {
            Lock lock = lockTable.get(variable).get(0);
            if (lock.getTransaction().equals(transaction)) { // there is only
                                                             // one lock and the
                                                             // same transaction
                                                             // holds it.
                return true;
            }
        }
        return false;
    }

    /**
     * transaction takes the write lock on the variable
     * 
     * @param transaction - the transaction
     * @param variable -the variable
     * 
     * @author Rachita & Anto
     */
    public void getWriteLock(Transaction transaction, String variable) {
        if (!lockTable.containsKey(variable)) {
            Lock lock = new Lock(transaction, LockType.WRITE);
            ArrayList<Lock> locks = new ArrayList<Lock>();
            locks.add(lock);
            lockTable.put(variable, locks);
        } else {
            Lock lock = lockTable.get(variable).get(0);
            lock.setType(LockType.WRITE); // convert read lock for the
                                          // transaction to write lock
        }

    }
    
    /**
     * returns true if transaction already has a write lock on the variable false otherwise
     * 
     * @param transaction -the transaction
     * @param variable -the variable
     * @return true or false
     * 
     * @author Rachita & Anto
     */
    public boolean isWriteLockTaken(Transaction transaction, String variable) {
        if (lockTable.containsKey(variable)) {
            Lock lock = lockTable.get(variable).get(0);
            if (lock.getTransaction().equals(transaction) && lock.getType().equals(LockType.WRITE)) {
                return true;
            }
        }
        return false;
    }

}
