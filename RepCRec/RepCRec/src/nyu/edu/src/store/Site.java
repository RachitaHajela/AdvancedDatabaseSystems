package nyu.edu.src.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import nyu.edu.src.lock.Lock;
import nyu.edu.src.lock.Lock.LockType;
import nyu.edu.src.transcation.Transaction;
import nyu.edu.src.transcation.TransactionManager;

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

    public void addVariableToSite(Variable variable) {
        variables.put(variable.getId(), variable);
    }

    public void addVariableToSite(int id, int value) {
        variables.put(id, new Variable(id, value));
    }

    public void addVariableToSite(String id, int value) {
        addVariableToSite(getWithoutStartingX(id), value);
    }

    public boolean variableExistsOnThisSite(String id) {
        return variables.containsKey(getWithoutStartingX(id));
    }

    public void failure(int timestamp) {
        status = ServerStatus.DOWN;
        lockTable.clear();
        variableHasRecovered.clear();
        previousFailtime = timestamp;
    }

    public void recover() {
        int id = this.getId();
        if (id % 2 == 0) {
            variableHasRecovered.add("x" + (id - 1));
            variableHasRecovered.add("x" + (id - 1 + 10));
        }
        status = ServerStatus.RECOVERING;

    }

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

    public String getVariable(int variableID) {
        if (!variables.containsKey(variableID)) {
            return "ignore";
        }

        return "x" + variableID + ":" + variables.get(variableID).getValue()
                + " ";
    }

    public int read(String id) {
        return read(getWithoutStartingX(id));
    }

    public int read(int id) {
        return variables.get(id).getValue();
    }

    public void write(int id, int value) {
        if (status.equals(ServerStatus.RECOVERING)) {
            variableHasRecovered.add("x" + id);
            if (variableHasRecovered.size() == variables.size()) {
                setStatus(ServerStatus.UP);
            }
        }
        addVariableToSite(id, value);
    }

    public void write(String id, int value) {
        write(getWithoutStartingX(id), value);
    }

    public int getWithoutStartingX(String id) {
        if (id.startsWith("x")) {
            int idWithoutX = Integer.parseInt(id.substring(1));
            return idWithoutX;
        }
        // ID was not properly provided (not in the form x1, x2, ..., x20)
        return -1;
    }

    public boolean transactionWaits(Transaction transaction, String variable) {
        ArrayList<Lock> locks = lockTable.get(variable);
        Transaction transHoldingLock = locks.get(0).getTransaction();
        if (transHoldingLock.getTimeStamp() < transaction.getTimeStamp()) {
            return false;
        }
        return true;
    }

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
     * returns true if transaction has a write lock on the variable
     * @param transaction
     * @param variable
     * @return true or false
     * @author RHAJELA
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
