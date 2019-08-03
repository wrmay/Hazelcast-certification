import com.hazelcast.certification.business.ruleengine.RuleEngine;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.certification.server.ProcessTransactionEntryProcessor;
import com.hazelcast.certification.util.TransactionsUtil;

import java.util.LinkedList;

public class ProcessingTimeTest {
    public static void main(String []args){
        // how long does it take for one thread to process 1000 transactions ?

        int HISTORICAL_TRANSACTIONS = 20;
        int TRANSACTIONS_TO_PROCESS = 10000;

        LinkedList<Transaction> historicalTransactions = new LinkedList<Transaction>();
        LinkedList<Transaction> transactionsToProcess = new LinkedList<Transaction>();


        for (int i = 0; i < HISTORICAL_TRANSACTIONS; ++i) {
            historicalTransactions.addLast(generateTransaction());
        }

        for(int i=0;i< TRANSACTIONS_TO_PROCESS; ++i){
            transactionsToProcess.addLast(generateTransaction());
        }

        System.out.println(String.format("Generated %d historical transactions and %d transactions to process.", HISTORICAL_TRANSACTIONS, TRANSACTIONS_TO_PROCESS));

        long start = System.currentTimeMillis();
        for (Transaction t : transactionsToProcess) {
            RuleEngine re = new RuleEngine(t, historicalTransactions);
            re.executeRules();
            historicalTransactions.removeFirst();
            historicalTransactions.addLast(t);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println(String.format("Processed %d transactions in %dms. Single thread speed limit is %d tps", TRANSACTIONS_TO_PROCESS, elapsed,TRANSACTIONS_TO_PROCESS*1000/elapsed));

    }

    private static TransactionsUtil txnUtil = new TransactionsUtil();

    private static Transaction generateTransaction(){

        String ccNumber = "00000000001234";
        String countryCode= txnUtil.generateCountryCode();
        long ts = txnUtil.generateTimeStamp();
        String responseCode = txnUtil.generateResponseCode(77);
        int amount = txnUtil.generateTxnAmount();
        String type = txnUtil.generateMerchantType();
        String city = txnUtil.generateCityCode();
        String txCode = txnUtil.generateTxnCode(44);

        StringBuffer txn = new StringBuffer();
        txn.append(ccNumber)
                .append(",").append(ts)
                .append(",").append(countryCode)
                .append(",").append(responseCode)
                .append(",").append(amount)
                .append(",").append(countryCode)  // yes, currency code == country code
                .append(",").append(type)
                .append(",").append(city)
                .append(",").append(txCode)
                .append(new String(new byte[99 - txn.toString().getBytes().length]))
                .append("\n");

        String rawTxnString = txn.toString();

        // from TransactionSource.process
        int z = rawTxnString.indexOf(0);
        String txnString = rawTxnString.substring(0, z);

        // from ProcessTransactionEntryProcessor
        ProcessTransactionEntryProcessor ep = new ProcessTransactionEntryProcessor(txnString);
        Transaction t = ep.prepareTransaction(txnString);

        return t;
    }
}
