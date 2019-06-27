package com.hazelcast.certification.util;

import com.hazelcast.certification.domain.Stats;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class Reporter {
    public static void main(String []args){
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        IMap<String, Stats> statsMap = hz.getMap("stats");
        Stats baseline = statsMap.aggregate(new StatsAggregator());
        Stats current;
        int txnsProcessed;

        while(true){
            try {
                Thread.sleep(10000);
            } catch(InterruptedException x){
                //
            }

            current = statsMap.aggregate(new StatsAggregator());
            txnsProcessed = current.getTransactionsScored() - baseline.getTransactionsScored();

            System.out.println(String.format("Total transactions processed: %d  Throughput in last 10 seconds: %d txns/sec",current.getTransactionsScored(), txnsProcessed/10));
            baseline = current;
        }
    }
}
