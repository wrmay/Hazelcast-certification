package com.hazelcast.certification.process.impl.executorService.data;

import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.ArrayList;
import java.util.List;

public class DataAccessManager {

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
		int index = allTxns.size();
		allTxns.add(index, currentTxn);
		map.set(currentTxn.getCreditCardNumber(), allTxns);
		allTxns.remove(index);
		return allTxns;
	}
}
