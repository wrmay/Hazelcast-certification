package com.hazelcast.certification.server;

import com.hazelcast.certification.business.ruleengine.RuleEngine;
import com.hazelcast.certification.domain.CreditCardKey;
import com.hazelcast.certification.domain.FraudCheck;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import java.util.LinkedList;
import java.util.Map;

public class ProcessTransactionEntryProcessor implements EntryProcessor<CreditCardKey, LinkedList<Transaction>>,
        EntryBackupProcessor<CreditCardKey, LinkedList<Transaction>>{

    public ProcessTransactionEntryProcessor(String transactionString){
        this.transactionString = transactionString;
    }

    private String transactionString;

    @Override
    public void processBackup(Map.Entry<CreditCardKey, LinkedList<Transaction>> entry) {
        process(entry);
    }

    @Override
    public Object process(Map.Entry<CreditCardKey, LinkedList<Transaction>> entry) {
        Transaction transaction = prepareTransaction(transactionString);
        LinkedList<Transaction> history = entry.getValue();
        history.add(transaction);
        history.removeFirst(); // keep it at a constant size
        RuleEngine re = new RuleEngine(transaction, history);
        re.executeRules();
        transaction.setFraudCheck(new FraudCheck(re.isFraudTxn(), re.getFailedTest()));
        entry.setValue(history); // so Hazelcast will know this is not a read only method
        return null;
    }

    @Override
    public EntryBackupProcessor<CreditCardKey, LinkedList<Transaction>> getBackupProcessor() {
        return this;
    }

    private Transaction prepareTransaction(String txnString) throws RuntimeException {
        Transaction txn = new Transaction();
        String[] cName = txnString.split(",");
        txn.setCreditCardNumber(cName[0]);
        txn.setTimeStamp(Long.parseLong(cName[1]));
        txn.setCountryCode(cName[2]);
        txn.setResponseCode(cName[3]);
        txn.setTxnAmt(Integer.parseInt(cName[4]));
        txn.setMerchantType(cName[6]);
        txn.setTxnCity(cName[7]);
        txn.setTxnCode(cName[8]);
        return txn;
    }

}
