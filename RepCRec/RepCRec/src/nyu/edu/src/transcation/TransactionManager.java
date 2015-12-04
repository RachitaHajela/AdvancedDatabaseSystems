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
	 * transaction makes a read request
	 * 
	 * @param transaction
	 * @param var
	 * @param timeStamp
	 */
	public void readRequest(Transaction transaction, Variable var, int timeStamp) {

	}

	/**
	 * transaction makes a write request
	 * 
	 * @param transaction
	 * @param var
	 * @param value
	 * @param timeStamp
	 */
	public void writeRequest(Transaction transaction, Variable var, int value,
			int timeStamp) {

	}

	/**
	 * transaction makes a commit request
	 * 
	 * @param transaction
	 * @param timeStamp
	 */
	public void commitRequest(Transaction transaction, int timeStamp) {

	}

	/**
	 * gives the committed values of all copies of all variables at all sites,
	 * sorted per site.
	 */
	public void dump() {

	}

	/**
	 * gives the committed values of all copies of all variables at site siteNUm
	 * 
	 * @param siteNum
	 */
	public void dump(int siteNum) {

	}

	/**
	 * gives the committed values of all copies of variable var at all sites.
	 * 
	 * @param var
	 */
	public void dump(String var) {

	}
}
