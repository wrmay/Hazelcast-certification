package com.hazelcast.certification.server;

import com.hazelcast.certification.domain.Stats;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import java.util.Map;

public class RecordResultsEntryProcessor implements EntryProcessor<String, Stats>, EntryBackupProcessor<String,Stats> {
    public Object process(Map.Entry<String, Stats> entry) {
        Stats stats = entry.getValue();
        if (stats == null) stats = new Stats();
        stats.registerResult();
        entry.setValue(stats);
        return null;
    }

    public EntryBackupProcessor<String, Stats> getBackupProcessor() {
        return this;
    }

    public void processBackup(Map.Entry<String, Stats> entry) {
        process(entry);
    }
}
