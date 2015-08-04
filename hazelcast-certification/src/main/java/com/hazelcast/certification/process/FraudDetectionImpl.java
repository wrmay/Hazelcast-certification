package com.hazelcast.certification.process;

import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

/**
 * This implementation assumes cluster in Client-Server setup. Other
 * implementations may not use Client-Server, create HazelcastInstance
 * accordingly. It uses <b>IExecutorService</b> to execute business
 * rules on the relevant cluster nodes where the data is stored.
 * @author rahul
 *
 */
public class FraudDetectionImpl extends com.hazelcast.certification.process.FraudDetection {

	private final static ILogger log = Logger.getLogger(FraudDetectionImpl.class);
	private static HazelcastInstance HAZELCAST;
	private static int EXECUTOR_POOL_SIZE;
	private final static String EXECUTOR_POOL_NAME = "FraudDetectionService";
	
	//Initializing Client with defaults, but add more specific configurations later.
	static {
		HAZELCAST = HazelcastClient.newHazelcastClient();
	}

	@Override
	protected void startFraudDetection() {
		EXECUTOR_POOL_SIZE = Integer.parseInt(System.getProperty("ExecutorPoolSize"));

		Config config = new Config();
		ExecutorConfig eConfig = config.getExecutorConfig(EXECUTOR_POOL_NAME);
		eConfig.setPoolSize(EXECUTOR_POOL_SIZE).setName(EXECUTOR_POOL_NAME);
		IExecutorService service = HAZELCAST.getExecutorService(EXECUTOR_POOL_NAME);
		
		while(!Thread.interrupted()) {
			try {
				Transaction txn = getNextTxn();
				if(txn != null) {
					//Future<Boolean> future = service.submitToKeyOwner(new com.hazelcast.certification.process.FraudDetectionTask(txn), getClusterKey(txn));
					//Future<Boolean> future = service.submit(new com.hazelcast.certification.process.FraudDetectionTask(txn));//, getClusterKey(txn));
					//log.info("Fraud transaction Credit Card ID:" + txn.getCreditCardNumber() + ": " + future.get());
					//future.get();

					service.executeOnKeyOwner(new com.hazelcast.certification.process.FraudDetectionTask(txn), getClusterKey(txn));
					getTPSCounter().incrementAndGet();
				}
			} catch (InterruptedException e) {
				log.severe(e);
			} catch (ArrayIndexOutOfBoundsException e) {
				log.info("Bad String received... discarding.");
			}
		}
	}

	private String getClusterKey(Transaction txn) {
		return txn.getCreditCardNumber();
	}
	
}