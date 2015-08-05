package com.hazelcast.certification.process;

import com.hazelcast.certification.business.ruleengine.RuleEngine;
import com.hazelcast.certification.data.DataAccessManager;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

public class FraudDetectionTask implements Serializable, HazelcastInstanceAware, Callable<Boolean> {
	
	private transient HazelcastInstance hazelcast;

	private Transaction txn;

	FraudDetectionTask(Transaction txn) {
		this.txn = txn;
	}
	
	public Boolean call() throws Exception {
	//public void run() {

		DataAccessManager dm = new DataAccessManager();
		dm.setHazelcastInstance(hazelcast);
		List<Transaction> allTxns = dm.updateAndGet(txn);
		
		RuleEngine ruleEngine = new RuleEngine(txn, allTxns);
		ruleEngine.executeRules();
		return ruleEngine.isFraudTxn();
	}

	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcast = hazelcastInstance;
	}
}
