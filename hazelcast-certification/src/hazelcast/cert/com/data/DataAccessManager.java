package hazelcast.cert.com.data;

import hazelcast.cert.com.domain.Transaction;

import java.util.List;
import java.util.Map;

/**
 * @author rahul
 *
 */
public interface DataAccessManager {
	
	/**
	 * Deletes all transactions for this credit card including 
	 * historical data 
	 * @param txn is the identification of the credit card 
	 * Credit Card ID number in this case
	 * @return true or false as the status of delete operation
	 */
	public boolean delete(Transaction txn);
	
	/**
	 * Deletes all records from the provided list, including 
	 * historical data of each of the credit cards used in the 
	 * list
	 * @param txns all transactions that need to be deleted
	 * @return true or false as the status of deleteAll operation
	 */
	public boolean deleteAll(List<Transaction> txns);

	/**
	 * Deletes all records in the list for the provided CreditCard Id
	 * @param txns all transactions that need to be deleted
	 * @return true or false as the status of deleteAll operation
	 */
	public boolean deleteAll(String creditCardID, List<Transaction> txns);
	
	/**
	 * Retrieves all historical transactions for this credit card ID
	 * @param id identification of the credit card used 
	 * in this transaction
	 * @return list of historic transactions of this credit card
	 */
	public List<Transaction> getHistoricalTransactions(String id);
	
	/**
	 * 
	 * @param currentTxn
	 * @return
	 */
	public List<Transaction> updateAndGet(Transaction currentTxn);
	
	/**
	 * 
	 * @param txns
	 * @return
	 */
	public void set(String creditCardID, List<Transaction> txns);
	
	/**
	 * 
	 * @param data
	 */
	public void setAll(Map<?, ?> data);
	
}
