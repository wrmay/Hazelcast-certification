package com.hazelcast.certification.server;

import com.hazelcast.certification.domain.CreditCardKey;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import java.util.LinkedList;
import java.util.Map;

public class AppendTransactionEntryProcessor implements EntryProcessor<CreditCardKey, LinkedList<Transaction>>,EntryBackupProcessor<CreditCardKey, LinkedList<Transaction>> {

    private Transaction t;

    public AppendTransactionEntryProcessor(Transaction t){
        this.t = t;
    }

    public Object process(Map.Entry<CreditCardKey, LinkedList<Transaction>> entry) {
        LinkedList<Transaction> history = entry.getValue();
        history.add(t);
        entry.setValue(history);   // this totally wont scale if a copy is being made here - need to find out
        return null;
    }

    public EntryBackupProcessor<CreditCardKey, LinkedList<Transaction>> getBackupProcessor() {
        return this;
    }

    public void processBackup(Map.Entry<CreditCardKey, LinkedList<Transaction>> entry) {
        process(entry);
    }
}
