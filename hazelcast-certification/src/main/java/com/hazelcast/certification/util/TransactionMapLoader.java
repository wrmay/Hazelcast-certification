package com.hazelcast.certification.util;

import com.hazelcast.certification.domain.CreditCardKey;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.core.MapLoader;

import java.util.*;

public class TransactionMapLoader implements MapLoader<CreditCardKey, LinkedList<Transaction>> {

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

    public LinkedList<Transaction> load(CreditCardKey key) {
        LinkedList<Transaction> result;
        synchronized (txnGenerator){
            result = txnGenerator.createAndGetCreditCardTransactions(key.getCardNumber(), historicalTransactionCount);
        }
        return result;
    }

    public Map<CreditCardKey, LinkedList<Transaction>> loadAll(Collection<CreditCardKey> collection) {
        HashMap<CreditCardKey, LinkedList<Transaction>> result = new HashMap<CreditCardKey, LinkedList<Transaction>>(collection.size());
        for(CreditCardKey cc: collection) result.put(cc, load(cc));
        return result;
    }

    public Iterable<CreditCardKey> loadAllKeys() {
        ArrayList<CreditCardKey> result = new ArrayList<CreditCardKey>(cardCount);
        synchronized (txnGenerator){
            for (int i = 0; i < cardCount; ++i) result.add(new CreditCardKey(txnGenerator.generateCreditCardNumber(i)));
        }
        return result;
    }
}
