package com.hazelcast.certification.process;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.certification.business.ruleengine.RuleEngine;
import com.hazelcast.certification.data.DataAccessManager;
import com.hazelcast.certification.domain.Transaction;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

public class FraudDetectionTask implements Callable<Boolean>, Serializable, HazelcastInstanceAware {
	
	private transient HazelcastInstance hazelcast;

	private Transaction txn;

	FraudDetectionTask(Transaction txn) {
		this.txn = txn;
	}
	
	@Override
	public Boolean call() throws Exception {

		DataAccessManager dm = new DataAccessManager();
		dm.setHazelcastInstance(hazelcast);
		List<Transaction> allTxns = dm.updateAndGet(txn);
		
		RuleEngine ruleEngine = new RuleEngine();
		ruleEngine.setRulesAttributes(txn, allTxns);
		ruleEngine.executeRules();
		return ruleEngine.isFraudTxn();
	}

	@Override
	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcast = hazelcastInstance;
	}
}
