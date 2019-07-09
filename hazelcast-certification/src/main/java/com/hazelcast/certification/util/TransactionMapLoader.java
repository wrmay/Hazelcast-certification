package com.hazelcast.certification.util;

import com.hazelcast.certification.domain.CreditCardKey;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.util.*;

public class TransactionMapLoader implements MapLoader<CreditCardKey, LinkedList<Transaction>>, MapLoaderLifecycleSupport {

    private static ILogger log = Logger.getLogger(TransactionMapLoader.class);

    private static final String PRELOAD_CARD_COUNT_PROPERTY = "preload.cardCount";
    private static final String PRELOAD_TXN_COUNT_PER_CARD = "preload.txnCount";

    // configuration
    private int cardCount;
    private int historicalTransactionCount;

    // state
    private  TransactionsUtil txnGenerator;


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


    @Override
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String s) {
        cardCount = parseRequiredIntegerProp(properties, PRELOAD_CARD_COUNT_PROPERTY);
        historicalTransactionCount = parseRequiredIntegerProp(properties, PRELOAD_TXN_COUNT_PER_CARD);

        txnGenerator = new TransactionsUtil();
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    private int parseRequiredIntegerProp(Properties props, String propName){
        String val = props.getProperty(propName);
        int result;
        if (val == null){
            throw new RuntimeException(String.format("Required property not present in map loader configuration: %s.",propName));
        } else {
            try {
                result = Integer.parseInt(val);
            } catch(NumberFormatException x){
                throw new RuntimeException(String.format("Value of % property could not be parsed as a number: %s", propName, val));
            }
        }

        return result;
    }
}
