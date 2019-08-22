package com.hazelcast.certification.util;

import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.certification.domain.TransactionHistoryContainer;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapLoaderLifecycleSupport;

import java.util.*;

public class TransactionMapLoader implements MapLoader<String, TransactionHistoryContainer>, MapLoaderLifecycleSupport {

    private static final String PRELOAD_CARD_COUNT_PROPERTY = "preload.cardCount";
    private static final String PRELOAD_TXN_COUNT_PER_CARD = "preload.txnCount";

    // configuration
    private int cardCount;
    private int historicalTransactionCount;

    // state
    private TransactionsUtil txnGenerator;


    public TransactionHistoryContainer load(String ccNumber) {
        TransactionHistoryContainer result = txnGenerator.createAndGetCreditCardTransactions(ccNumber, historicalTransactionCount);
        return result;
    }

    public Map<String, TransactionHistoryContainer> loadAll(Collection<String> keys) {
        HashMap<String, TransactionHistoryContainer> result = new HashMap<>(keys.size());
        for (String cc : keys) result.put(cc, load(cc));
        return result;
    }

    public Iterable<String> loadAllKeys() {
        ArrayList<String> result = new ArrayList<>(cardCount);
        synchronized (txnGenerator) {
            for (int i = 0; i < cardCount; ++i) result.add(txnGenerator.generateCreditCardNumber(i));
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

    private int parseRequiredIntegerProp(Properties props, String propName) {
        String val = props.getProperty(propName);
        int result;
        if (val == null) {
            throw new RuntimeException(String.format("Required property not present in map loader configuration: %s.", propName));
        } else {
            try {
                result = Integer.parseInt(val);
            } catch (NumberFormatException x) {
                throw new RuntimeException(String.format("Value of %s property could not be parsed as a number: %s", propName, val));
            }
        }

        return result;
    }
}
