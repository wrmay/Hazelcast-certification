package com.hazelcast.certification.process.impl;

import com.hazelcast.certification.domain.Result;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.core.ExecutionCallback;
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
	private final static String EXECUTOR_POOL_NAME = "FraudDetectionService";
	
	//Initializing Client with defaults, but add more specific configurations later.
	static {
		HAZELCAST = HazelcastClient.newHazelcastClient();
	}

	@Override
	protected void startFraudDetection() {
		int EXECUTOR_POOL_SIZE = Integer.parseInt(System.getProperty("ExecutorPoolSize"));

		Config config = new Config();
		ExecutorConfig eConfig = config.getExecutorConfig(EXECUTOR_POOL_NAME);
		eConfig.setPoolSize(EXECUTOR_POOL_SIZE).setName(EXECUTOR_POOL_NAME);
		IExecutorService service = HAZELCAST.getExecutorService(EXECUTOR_POOL_NAME);
		ExecutionCallback<Result> callback = new ExecutionCallback<Result>() {
			public void onResponse(Result o) {
				getTPSCounter().incrementAndGet();
			}

			public void onFailure(Throwable throwable) {
				log.warning("Executor task failure: "+throwable.getMessage());
			}
		};
		
		while(!Thread.interrupted()) {
			try {
				Transaction txn = getNextTxn();
				if(txn != null) {
					service.submitToKeyOwner(new FraudDetectionTask(txn), getClusterKey(txn), callback);
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