package com.hazelcast.certification.server;

import com.hazelcast.certification.domain.TransactionHistoryContainer;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import java.util.Map;

public class TransactionHistoryPurgeEntryProcessor implements EntryProcessor<String, TransactionHistoryContainer>, EntryBackupProcessor<String, TransactionHistoryContainer> {

    @Override
    public void processBackup(Map.Entry<String, TransactionHistoryContainer> entry) {
        process(entry);
    }

    @Override
    public Object process(Map.Entry<String, TransactionHistoryContainer> entry) {
        TransactionHistoryContainer container = entry.getValue();
        int deleted = container.purge();
        entry.setValue(container);
        return Integer.valueOf(deleted);
    }

    @Override
    public EntryBackupProcessor<String, TransactionHistoryContainer> getBackupProcessor() {
        return this;
    }
}
