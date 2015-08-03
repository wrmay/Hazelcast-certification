package com.hazelcast.certification.data;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.certification.domain.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataAccessManager {

	private final static ILogger log = Logger.getLogger(DataAccessManager.class);

	private HazelcastInstance hazelcast;

	private static final String HAZELCAST_MAP_NAME = "CreditCardCache";

	public void setHazelcastInstance(HazelcastInstance hazelcast) {
		this.hazelcast = hazelcast;
	}

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

	public void set(String creditCardID, List<Transaction> txns) {
		IMap<String, List<Transaction>> map = hazelcast.getMap(HAZELCAST_MAP_NAME);
		map.set(creditCardID, txns);
	}

	public void setAll(Map<?, ?> data) {
		hazelcast.getMap(HAZELCAST_MAP_NAME).putAll(data);
	}

}
