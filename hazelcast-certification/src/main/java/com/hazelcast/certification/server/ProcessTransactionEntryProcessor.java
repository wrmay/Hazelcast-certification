package com.hazelcast.certification.server;

import com.hazelcast.certification.business.ruleengine.RuleEngine;
import com.hazelcast.certification.domain.FraudCheck;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.certification.domain.TransactionHistoryContainer;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import java.util.Map;

import io.prometheus.client.Counter;

public class ProcessTransactionEntryProcessor implements EntryProcessor<String, TransactionHistoryContainer>,
        EntryBackupProcessor<String, TransactionHistoryContainer> {

    private static ILogger log = Logger.getLogger(ProcessTransactionEntryProcessor.class);

    public ProcessTransactionEntryProcessor(String transactionString){
        this.transactionString = transactionString;
    }

    private String transactionString;

    private static final Counter transactionsProcessed = Counter.build().name("transactions_processed_total").help("total transactions processed").register();
    private static final Counter transactionProcessingExceptions = Counter.build().name("transaction_processing_exceptions_total").help("total transaction processing exceptions").register();


    @Override
    public void processBackup(Map.Entry<String,TransactionHistoryContainer> entry) {
        doProcessEntry(entry);
    }

    @Override
    public Object process(Map.Entry<String, TransactionHistoryContainer> entry) {
        Object result = doProcessEntry(entry);
        transactionsProcessed.inc();   // be careful not to count backup entries
        return result;
    }

    private Object doProcessEntry(Map.Entry<String, TransactionHistoryContainer> entry) {
        try {
            Transaction transaction = prepareTransaction(transactionString);
            TransactionHistoryContainer history = entry.getValue();
            if (history == null) log.warning("HISTORY IS NULL");
            history.add(transaction);
            RuleEngine re = new RuleEngine(transaction, history);
            re.executeRules();
            history.remove(transaction);  // this is done only for a test so the size will not grow over time
            transaction.setFraudCheck(new FraudCheck(re.isFraudTxn(), re.getFailedTest()));
            entry.setValue(history); // so Hazelcast will know this is not a read only method
        } catch(Exception x){
            log.warning("Exception during processing", x);
            transactionProcessingExceptions.inc();
            throw x;
        }
        return null;
    }


    @Override
    public EntryBackupProcessor<String, TransactionHistoryContainer> getBackupProcessor() {
        return this;
    }

    /*
     * this is public only so it can be tested
     */
    public  Transaction prepareTransaction(String txnString) throws RuntimeException {
        Transaction txn = new Transaction();
        String[] cName = txnString.split(",");
        txn.setCreditCardNumber(cName[0]);
        txn.setTimeStamp(Long.parseLong(cName[1]));
        txn.setCountryCode(cName[2]);
        txn.setResponseCode(cName[3]);
        txn.setTxnAmt(Integer.parseInt(cName[4]));
        txn.setTxnCurrency(cName[5]);
        txn.setMerchantType(cName[6]);
        txn.setTxnCity(cName[7]);
        txn.setTxnCode(cName[8]);
        return txn;
    }

}
