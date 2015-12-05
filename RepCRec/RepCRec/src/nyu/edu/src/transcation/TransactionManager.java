package nyu.edu.src.transcation;

import java.util.List;

import nyu.edu.src.store.Site;
import nyu.edu.src.store.Variable;

public class TransactionManager {

	public static final int numberOfTotalSites = 10;
	public static final int numberOfTotalVariables = 20;

	private List<Site> sites;
	private List<Transaction> transactions;
	private List<Transaction> waitingTransactions;

	/**
	 * sets up the initial database: sites with variables and their initial
	 * values
	 */
	public void setUp() {

	}


	/**
	 * transaction makes a commit request
	 * 
	 * @param transaction
	 * @param timeStamp
	 */
	public void commitRequest(Transaction transaction, int timestamp) {
	    System.out.println("COMMIT : timestamp = " + timestamp + ", transaction = " + transaction);
	}
	
	/**
	 * transaction begins
	 * 
	 * @param timeStamp
	 * @param transaction
	 */
	public void begin(int timestamp, String transaction) {
	    System.out.println("BEGIN : timestamp = " + timestamp + ", transaction = " + transaction);
	}
	
	/**
	 * transaction begins in Read-Only mode
	 * 
	 * @param timeStamp
	 * @param transaction
	 */
	public void beginRO(int timestamp, String transaction) {
	    System.out.println("BEGINRO : timestamp = " + timestamp + ", transaction = " + transaction);
	}
	
	/**
	 * transaction ends
	 * 
	 * @param timeStamp
	 * @param transaction
	 */
	public void end(int timestamp, String transaction) {
	    System.out.println("END : timestamp = " + timestamp + ", transaction = " + transaction);
	}
	
	/**
	 * site fails
	 * 
	 * @param timeStamp
	 * @param siteID
	 */
	public void fail(int timestamp, int siteID) {
	    System.out.println("FAIL : timestamp = " + timestamp + ", siteID = " + siteID);
	}
	
	/**
	 * site recovers
	 * 
	 * @param timeStamp
	 * @param siteID
	 */
	public void recover(int siteID) {
	    System.out.println("RECOVER : siteID = " + siteID);
	}
	
	/**
	 * transaction makes a write request
	 * 
	 * @param timeStamp
	 * @param transaction
	 * @param variable
	 * @param val
	 */
	public void writeRequest(int timestamp, String transaction, String variable, String val) {
	    int value = Integer.parseInt(val);
	    System.out.println("WRITE : timestamp = " + timestamp + ", transaction = " + transaction + 
		    ", variable = " + variable + ", value = " + value);
	}
	
	/**
	 * transaction makes a read request
	 * 
	 * @param timeStamp
	 * @param transaction
	 * @param variable
	 */
	public void readRequest(int timestamp, String transaction, String variable) {
	    System.out.println("READ : timestamp = " + timestamp + ", transaction = " + transaction + 
		    ", variable = " + variable);
	}
	
	/**
	 * gives the committed values of all copies of all variables at all sites,
	 * sorted per site.
	 */
	public void dump() {
	    System.out.println("DUMP :");
	}

	/**
	 * gives the committed values of all copies of all variables at site siteNUm
	 * 
	 * @param siteNum
	 */
	public void dump(int siteNum) {
	    System.out.println("DUMP : siteNum = " + siteNum);
	}

	/**
	 * gives the committed values of all copies of variable var at all sites.
	 * 
	 * @param var
	 */
	public void dump(String var) {

	}
}
