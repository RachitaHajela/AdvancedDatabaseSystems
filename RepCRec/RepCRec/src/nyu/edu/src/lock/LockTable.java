package nyu.edu.src.lock;

import java.util.HashMap;
import java.util.Map;

public class LockTable {
    private Map<Integer, Lock> lockTable;

    public LockTable() {
	lockTable = new HashMap<Integer, Lock>();
    }

    public Map<Integer, Lock> getLockTable() {
	return lockTable;
    }

}
