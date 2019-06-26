package com.hazelcast.certification.util;

import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

import java.util.List;

public class Loader {
    public static void main(String []args){
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        MultiMap<String, List<Transaction>> transactions = hz.getMultiMap("transactions");

        TransactionsUtil txnGenerator = new TransactionsUtil();
        int cards = 1000000;
        int txnsPerCard = 20;

        for(int i=0;i < cards; ++i){
            String cc = txnGenerator.generateCreditCardNumber(0);
            List<Transaction> transactionList = txnGenerator.createAndGetCreditCardTransactions(cc, txnsPerCard);

            transactions.put(cc, transactionList);
            if (i % 10000 == 9999) System.out.println(String.format("Loaded %8d cards", i + 1));
        }

        hz.shutdown();
    }
}
