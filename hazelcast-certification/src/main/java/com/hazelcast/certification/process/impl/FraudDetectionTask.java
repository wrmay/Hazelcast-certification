package com.hazelcast.certification.process.impl;

import com.hazelcast.certification.business.ruleengine.RuleEngine;
import com.hazelcast.certification.data.DataAccessManager;
import com.hazelcast.certification.domain.Result;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

public class FraudDetectionTask implements Serializable, HazelcastInstanceAware, Callable<Result> {

	private static final long serialVersionUID = 4014524872106840633L;

	private transient HazelcastInstance hazelcast;

	private Transaction txn;

	FraudDetectionTask(Transaction txn) {
		this.txn = txn;
	}
	
	public Result call() throws Exception {

		DataAccessManager dm = new DataAccessManager();
		dm.setHazelcastInstance(hazelcast);
		List<Transaction> allTxns = dm.updateAndGet(txn);
		
		RuleEngine ruleEngine = new RuleEngine(txn, allTxns);
		ruleEngine.executeRules();

		Result result = new Result();
		result.setCreditCardNumber(txn.getCreditCardNumber());
		result.setFraudTransaction(ruleEngine.isFraudTxn());

		return result;
	}

	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcast = hazelcastInstance;
	}
}
