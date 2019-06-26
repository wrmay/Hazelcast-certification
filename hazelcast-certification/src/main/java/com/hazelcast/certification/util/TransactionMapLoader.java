package com.hazelcast.certification.util;

import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.core.MapLoader;

import java.util.*;

public class TransactionMapLoader implements MapLoader<String, LinkedList<Transaction>> {

    private static final String PRELOAD_CARD_COUNT_PROPERTY = "preload.cardCount";
    private static final String PRELOAD_TXN_COUNT_PER_CARD = "preload.txnCount";

    private int cardCount = 100000;
    private int historicalTransactionCount = 20;
    private final TransactionsUtil txnGenerator;

    public TransactionMapLoader(){
        String cardCountSetting = System.getProperty(PRELOAD_CARD_COUNT_PROPERTY);
        String txnCountSetting = System.getProperty(PRELOAD_TXN_COUNT_PER_CARD);

        if (cardCountSetting != null){
            try {
                cardCount = Integer.parseInt(cardCountSetting);
            } catch(NumberFormatException x){
                // log something here
            }
        }

        if (txnCountSetting != null) {
            try {
                historicalTransactionCount = Integer.parseInt(txnCountSetting);
            } catch(NumberFormatException x){
                // log something here
            }
        }

        txnGenerator = new TransactionsUtil();
    }

    public LinkedList<Transaction> load(String s) {
        LinkedList<Transaction> result;
        synchronized (txnGenerator){
            result = txnGenerator.createAndGetCreditCardTransactions(s, historicalTransactionCount);
        }
        return result;
    }

    public Map<String, LinkedList<Transaction>> loadAll(Collection<String> collection) {
        HashMap<String, LinkedList<Transaction>> result = new HashMap<String, LinkedList<Transaction>>(collection.size());
        for(String cc: collection) result.put(cc, load(cc));
        return result;
    }

    public Iterable<String> loadAllKeys() {
        ArrayList<String> result = new ArrayList<String>(cardCount);
        synchronized (txnGenerator){
            for (int i = 0; i < cardCount; ++i) result.add(txnGenerator.generateCreditCardNumber(i));
        }
        return result;
    }
}
