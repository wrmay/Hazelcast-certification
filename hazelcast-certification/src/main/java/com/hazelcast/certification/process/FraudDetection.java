package com.hazelcast.certification.process;

import com.hazelcast.certification.domain.Result;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.map.merge.PassThroughMergePolicy;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * This provides framework of Fraud Detection. To begin Fraud Detection process,
 * an implementation must be provided that extends this class. The application
 * invokes <code>startFraudDetection()</code> that is expected to contain the
 * implementation of Fraud Detection process. This method is invoked only once
 * and the application will wait if no implementation provided.
 *
 * <br>
 * To obtain next transaction available for Fraud Detection, invoke <code>getNextTransaction()</code>.
 * For every transaction that is consumed for Fraud Detection, <code>registerResult()</code> must be
 * called otherwise the transaction stands incomplete.
 *
 * <br>
 * This class also prints TPS. Frequency of printing TPS or TPS Interval is configurable,
 * see <I>FraudDetection.properties</I> for detail. Default TPS Interval is 5 seconds.
 */
public abstract class FraudDetection extends Thread {

    private final static ILogger log = Logger.getLogger(FraudDetection.class);

    private long startTime;
    private AtomicInteger txnCount;
    protected BlockingQueue<String> txnQueue;

    final public void run() {
        txnCount = new AtomicInteger(0);
        startTime = System.currentTimeMillis();
        startPerformanceReporter();

        Transaction t;
        try {
            t = getNextTransaction();
        } catch(InterruptedException x){
            return; // RETURN
        }

        while (!Thread.interrupted()){

            handle(t);

            try {
                t = getNextTransaction();
            } catch(InterruptedException x){
                return; // RETURN
            }
        }
    }

    protected abstract void handle(Transaction t);


    public void shutdown() {
        this.interrupt();
        try {
            this.join(10000);
        } catch (InterruptedException x) {
            log.warning("Could not verify shutdown of fraud detection");
        }

        // final report
        long count = txnCount.get();
        long now = System.currentTimeMillis();
        long elapsed = now - startTime;
        int queueSize = txnQueue.size();

        long tps = (count * 1000) / elapsed;

        System.out.println("Queue Size is: " + queueSize);
        System.out.println("Transactions Processed: " + count);
        System.out.println("Elapsed time: " + elapsed / 1000 + "s");
        System.out.println("Overall TPS is: " + tps);

    }

    public void bindTransactionQueue(BlockingQueue<String> queue) {
        this.txnQueue = queue;
    }

    private void startPerformanceReporter() {
        Thread reporter = new Thread() {
            @Override
            public void run() {
                long countAsOfLastTime = 0L;
                long timeAsOfLastTime = System.currentTimeMillis();
                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException x) {
                        // not a problem
                    }

                    long count = txnCount.get();
                    long intervalCount = count - countAsOfLastTime;
                    long now = System.currentTimeMillis();
                    long elapsed = now - startTime;
                    long intervalDuration = now - timeAsOfLastTime;
                    int queueSize = txnQueue.size();

                    long tps = (count * 1000) / elapsed;
                    long intervalTPS = (intervalCount * 1000) / intervalDuration;

                    System.out.println("Queue Size is: " + queueSize);
                    System.out.println("Transactions Processed: " + count);
                    System.out.println("Elapsed time: " + elapsed / 1000 + "s");
                    System.out.println("TPS over the last interval: " + intervalTPS);
                    System.out.println("Overall TPS is: " + tps);

                    countAsOfLastTime = count;
                    timeAsOfLastTime = now;
                }

            }
        };
        reporter.setDaemon(true);
        reporter.start();
    }

    final protected void registerResult(Result result) {
        if (isValidResult(result)) {
            txnCount.incrementAndGet();
        } else {
            log.warning("INVALID RESULT");
        }
    }

    private boolean isValidResult(Result result) {
        return result.getCreditCardNumber() != null;
    }

    final protected Transaction getNextTransaction() throws InterruptedException {
        return prepareTransaction(txnQueue.take());
    }

    // candidate for pooling
    final protected Transaction prepareTransaction(String txnString) throws RuntimeException {
        Transaction txn = new Transaction();
        String[] cName = txnString.split(",");
        txn.setCreditCardNumber(cName[0]);
        txn.setTimeStamp(Long.parseLong(cName[1]));
        txn.setCountryCode(cName[2]);
        txn.setResponseCode(cName[3]);
        txn.setTxnAmt(cName[4]);
        txn.setMerchantType(cName[6]);
        txn.setTxnCity(cName[7]);
        txn.setTxnCode(cName[8]);
        return txn;
    }


}
