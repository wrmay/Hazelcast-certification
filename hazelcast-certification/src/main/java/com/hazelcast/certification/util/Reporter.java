package com.hazelcast.certification.util;

import com.hazelcast.certification.domain.CreditCardKey;
import com.hazelcast.certification.domain.Stats;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.LinkedList;

public class Reporter {
    public static void main(String []args){
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        IMap<CreditCardKey, LinkedList<Transaction>> statsMap = hz.getMap("transaction_history");
        long now = System.currentTimeMillis();
        Stats baseline = statsMap.aggregate(new StatsAggregator(now - 10000));
        Stats current;

        while(true){
            try {
                Thread.sleep(10000);
            } catch(InterruptedException x){
                //
            }

            now = System.currentTimeMillis();
            current = statsMap.aggregate(new StatsAggregator(now - 10000));

            System.out.println(String.format("Transactions (fraudulent) processed in the last 10 seconds: %d (%d)", current.getTransactionsScored(), current.getFraudulentTransactions()));
            baseline = current;
        }
    }
}
