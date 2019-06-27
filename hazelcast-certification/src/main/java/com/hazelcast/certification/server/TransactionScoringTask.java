package com.hazelcast.certification.server;

import com.hazelcast.certification.business.ruleengine.RuleEngine;
import com.hazelcast.certification.domain.CreditCardKey;
import com.hazelcast.certification.domain.Stats;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class TransactionScoringTask implements Runnable, Serializable, HazelcastInstanceAware {

    private Transaction txn;
    private HazelcastInstance hz;

    public TransactionScoringTask(Transaction t){
        txn = t;
    }

    public void run(){
        ILogger log = Logger.getLogger(TransactionScoringTask.class);
        // can I get a local entries only map??
        IMap<CreditCardKey, LinkedList<Transaction>> transactionHistoryMap = hz.getMap("transaction_history");

        CreditCardKey ccKey = new CreditCardKey(txn.getCreditCardNumber());

        List<Transaction> history = transactionHistoryMap.get(ccKey);
        RuleEngine rules = new RuleEngine(txn, history);
        rules.executeRules();

        transactionHistoryMap.executeOnKey(ccKey, new AppendTransactionEntryProcessor(txn));

        IMap<String, Stats> resultMap = hz.getMap("stats");
        resultMap.executeOnKey(ccKey.getPartitionKey(), new RecordResultsEntryProcessor());
    }

    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        hz = hazelcastInstance;
    }
}
