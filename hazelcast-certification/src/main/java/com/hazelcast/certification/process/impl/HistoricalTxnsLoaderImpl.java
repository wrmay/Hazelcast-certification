package com.hazelcast.certification.process.impl;

import com.hazelcast.certification.process.HistoricalTransactionsLoader;
import com.hazelcast.certification.util.TransactionsUtil;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.certification.domain.Transaction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoricalTxnsLoaderImpl implements HistoricalTransactionsLoader {
	
	private final static ILogger log = Logger.getLogger(HistoricalTxnsLoaderImpl.class);

	private int TOTAL_CREDIT_CARDS, LOADER_THREAD_COUNT, TRANSACTIONS_PER_CARD;
	private boolean BULK_UPLOAD_ENABLED; 
	private int BULK_UPLOAD_BATCH_SIZE;
	private String HAZELCAST_MAP_NAME;
	
	private HazelcastInstance hazelcast;
		
	HistoricalTxnsLoaderImpl() {
		loadProperties();
		initialize();
	}
	
	private void loadProperties() {
		String propFileName = "FraudDetection.properties";
		InputStream stream = HistoricalTxnsLoaderImpl.class.getClassLoader().getResourceAsStream(propFileName);
		if (null == stream) {
			try {
				throw new FileNotFoundException("Property file " + propFileName
						+ " not found in the classpath");
			} catch (FileNotFoundException e) {
				log.severe(e);
			}
		}
		try {
			Properties properties = new Properties();
			properties.load(stream);
			setProperties(properties);
		} catch (IOException e) {
			log.severe(e);
		}
	}


	private void setProperties(Properties properties) {
		
		String temp = properties.getProperty("HistoricalCreditCardCount");
		if (temp == null) {
			log.severe("No Credit Card count provided to load as historical data. "
					+ "Configure HistoricalCreditCardCount. Exiting...");
			System.exit(0);
		}
		TOTAL_CREDIT_CARDS = Integer.parseInt(temp);
		
		temp = properties.getProperty("LoaderThreadCount");
		if(temp == null) {
			log.warning("No LoaderThreadCount provided. Using default of 1");
			LOADER_THREAD_COUNT = 1;
		} else
			LOADER_THREAD_COUNT = Integer.parseInt(temp);
		
		temp = properties.getProperty("TransactionsPerCard");
		if(temp == null) {
			log.warning("No TransactionsPerCard provided. Using default of 10");
			TRANSACTIONS_PER_CARD = 10;
		} else
			TRANSACTIONS_PER_CARD = Integer.parseInt(temp);
		
		temp = properties.getProperty("BulkUpload");
		if(temp == null) {
			log.warning("No configuration for BulkUpload provided. Using non-Bulk load mode");
			BULK_UPLOAD_ENABLED = false;
		} else
			BULK_UPLOAD_ENABLED = Boolean.getBoolean(temp);
		
		temp = properties.getProperty("BulkUploadBatchSize");
		if(temp == null && BULK_UPLOAD_ENABLED) {
			log.warning("Bulk Upload enabled but no configuration for BulkUploadBatchSize provided. Using default of 1000");
			BULK_UPLOAD_BATCH_SIZE = 1000;
		} else if(temp != null && BULK_UPLOAD_ENABLED)
			BULK_UPLOAD_BATCH_SIZE = Integer.parseInt(temp);
		
		temp = properties.getProperty("MapName");
		if(temp == null) {
			log.warning("No MapName provided. Exiting...");
			System.exit(0);
		}
		HAZELCAST_MAP_NAME = temp;
	}

	private void initialize() {
		hazelcast = HazelcastClient.newHazelcastClient();
	}

	public static void main(String args[]) {
		new HistoricalTxnsLoaderImpl().loadHistoricalTransactions();
	}


	/**
	 * Generates credit card accounts and historical transactions for each
	 * credit card. Each Credit Card is a unique account number. It takes a
	 * start point for credit card number and total cards to be created
	 */
	public void loadHistoricalTransactions() {
		log.info("Starting to load historical data in Hazelcast servers");
		CountDownLatch latch = new CountDownLatch(LOADER_THREAD_COUNT);
		ExecutorService service = Executors.newFixedThreadPool(LOADER_THREAD_COUNT);
		final int perThread = TOTAL_CREDIT_CARDS/LOADER_THREAD_COUNT;
		for(int i=0; i< LOADER_THREAD_COUNT; i++) {
			int start = perThread * i;
			int end = perThread * (i+1);
			service.execute(new Loader(start, end, latch));
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			log.severe(e);
		}
		service.shutdown();
		log.info("Data upload complete. Exiting now.");
		System.exit(0);
	}
	
	private class Loader implements Runnable{
		private int start;
		private int end;
		private CountDownLatch latch;
		
		Loader(int start, int end, CountDownLatch latch) {
			this.start = start;
			this.end = end;
			this.latch = latch;
		}
		
		public void run() {
			int putAllCounter = 0;
			Map<String, List<Transaction>> localMap = new HashMap<String, List<Transaction>>();
			TransactionsUtil txnUtil = new TransactionsUtil();
			for (int i = start; i < end; i++) {
				String creditCardNumber = txnUtil.generateCreditCardNumber(i);
				List<Transaction> cardTxns = txnUtil.createAndGetCreditCardTransactions(creditCardNumber, TRANSACTIONS_PER_CARD);
				if(BULK_UPLOAD_ENABLED) {
					if(putAllCounter < BULK_UPLOAD_BATCH_SIZE) {
						localMap.put(creditCardNumber, cardTxns);
					}
					if(putAllCounter == BULK_UPLOAD_BATCH_SIZE) {
						putInHazelcast(localMap);
						localMap.clear();
					}
					putAllCounter++;
					continue;
				}
				putInHazelcast(creditCardNumber, cardTxns);
			}
			latch.countDown();
		}
		
		private void putInHazelcast(String creditCardNumber, List<Transaction> allTxns) {
			hazelcast.getMap(HAZELCAST_MAP_NAME).set(creditCardNumber, allTxns);
		}
		
		private void putInHazelcast(Map<String, List<Transaction>> data) {
			hazelcast.getMap(HAZELCAST_MAP_NAME).putAll(data);
		}
	}
}
