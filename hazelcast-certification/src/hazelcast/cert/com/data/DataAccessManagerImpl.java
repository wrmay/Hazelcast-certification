package hazelcast.cert.com.data;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import hazelcast.cert.com.domain.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataAccessManagerImpl implements DataAccessManager, HazelcastInstanceAware {

	//private final static ILogger log = Logger.getLogger(DataAccessManagerImpl.class);
	
	protected HazelcastInstance hazelcast;
	private static final String HAZELCAST_MAP_NAME = "CreditCardCache";

	@Override
	public boolean delete(Transaction txn) {
		
		return false;
	}

	@Override
	public boolean deleteAll(List<Transaction> txns) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean deleteAll(String creditCardID, List<Transaction> txns) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Transaction> getHistoricalTransactions(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Transaction> updateAndGet(Transaction currentTxn) {
		IMap<String, List<Transaction>> map = hazelcast.getMap(HAZELCAST_MAP_NAME);
		List<Transaction> allTxns = map.get(currentTxn.getCreditCardNumber());
		if(allTxns == null) {
			allTxns = new ArrayList<Transaction>();
		}
		allTxns.add(currentTxn);
		map.set(currentTxn.getCreditCardNumber(), allTxns);
		return allTxns;
	}

	@Override
	public void set(String creditCardID, List<Transaction> txns) {
		IMap<String, List<Transaction>> map = hazelcast.getMap(HAZELCAST_MAP_NAME);
		map.set(creditCardID, txns);
	}

	@Override
	public void setHazelcastInstance(HazelcastInstance arg0) {
		hazelcast = arg0;


	}

	@Override
	public void setAll(Map<?, ?> data) {
		hazelcast.getMap(HAZELCAST_MAP_NAME).putAll(data);
	}
	
	private void temp() {
		
	}
}
