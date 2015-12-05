package nyu.edu.src.lock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LockTable {

	private Map<String, List<Lock>> lockTable;

	public LockTable() {
		lockTable = new HashMap<String, List<Lock>>();
	}

	public Map<String, List<Lock>> getLockTable() {
		return lockTable;
	}

}
