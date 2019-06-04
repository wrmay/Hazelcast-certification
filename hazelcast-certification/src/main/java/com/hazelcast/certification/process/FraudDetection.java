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
 *     This provides framework of Fraud Detection. To begin Fraud Detection process,
 *     an implementation must be provided that extends this class. The application
 *     invokes <code>startFraudDetection()</code> that is expected to contain the
 *     implementation of Fraud Detection process. This method is invoked only once
 *     and the application will wait if no implementation provided.
 *
 * <br>
 *     To obtain next transaction available for Fraud Detection, invoke <code>getNextTransaction()</code>.
 *     For every transaction that is consumed for Fraud Detection, <code>registerResult()</code> must be
 *     called otherwise the transaction stands incomplete.
 *
 * <br>
 *     This class also prints TPS. Frequency of printing TPS or TPS Interval is configurable,
 *     see <I>FraudDetection.properties</I> for detail. Default TPS Interval is 5 seconds.
 *
 */
public abstract class FraudDetection {

	private final static ILogger log = Logger.getLogger(FraudDetection.class);

	private AtomicInteger tpsCounter;
	protected BlockingQueue<String> txnQueue;
	private List<Integer> allTPSList;

	final public void run() {
		startPerformanceMonitor();
		startFraudDetection();
	}

	public void setAllTPSList(List<Integer> allTPSList) {
		this.allTPSList = allTPSList;
	}
	
	public void bindTransactionQueue(BlockingQueue<String> queue) {
		this.txnQueue = queue;
	}

	private void startPerformanceMonitor() {
		tpsCounter = new AtomicInteger();
		startTPSMonitor();
	}

	private void startTPSMonitor() {
		final int tpsInterval = Integer.parseInt(System.getProperty("TPSInterval"));
		Thread monitor = new Thread() {
			public void run() {
				try {
					while (!Thread.interrupted()) {
						sleep(tpsInterval * 1000);
						allTPSList.add(tpsCounter.get() / tpsInterval);
						log.info("Cluster statistics:\nTransactions pending for Fraud Detection = "+ txnQueue.size()+"\n" +
						"Transactions processed per second = "
								+ (tpsCounter.getAndSet(0) / tpsInterval));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		monitor.setDaemon(true);
		monitor.start();
	}

	final protected void registerResult(Result result) {
		if(isValidResult(result)) {
			tpsCounter.incrementAndGet();
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

	protected abstract void startFraudDetection();

}
