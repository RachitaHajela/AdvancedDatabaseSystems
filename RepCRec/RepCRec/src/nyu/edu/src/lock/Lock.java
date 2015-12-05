package nyu.edu.src.lock;

/**
 */
public class Lock {

    public enum LockType {
	READ, WRITE;
    }

    LockType type;
}
