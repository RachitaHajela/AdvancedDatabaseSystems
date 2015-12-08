package nyu.edu.src.lock;

import nyu.edu.src.transaction.Transaction;

/**
 */
public class Lock {

    /**
     * This class defines the Lock. A Lock is defined by the type of the lock
     * and also the transaction that holds that lock.
     * 
     * @author Rachita & Anto
     */

    public enum LockType {
        READ, WRITE;
    }

    LockType type;
    Transaction transaction;

    public Lock(Transaction trans, LockType type) {
        this.type = type;
        this.transaction = trans;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public LockType getType() {
        return type;
    }

    public void setType(LockType type) {
        this.type = type;
    }

}
