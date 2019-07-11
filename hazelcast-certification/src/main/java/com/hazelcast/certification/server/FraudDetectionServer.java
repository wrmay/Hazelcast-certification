package com.hazelcast.certification.server;

import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;

/**
 *
 */
public class FraudDetectionServer {

	private final static ILogger log = Logger.getLogger(FraudDetectionServer.class);

	// constants
	private static final String TRANSACTION_SERVER_HOST_PROP  = "transaction.server.host";
	private static final String TRANSACTION_SERVER_PORT_PROP  = "transaction.server.port";
	private static final String TRANSACTION_READER_THREADS_PROP  = "transaction.reader.threads";

	// configuration
	private int transactionServerPort;
	private String transactionServerHost;
	private int transactionReaderThreads;

	// state
	private HazelcastInstance hazelcast;
	private TransactionSource []transactionSources;

	public void start() throws IOException {
		hazelcast = Hazelcast.newHazelcastInstance();

		configure(hazelcast.getConfig().getProperties());

		// wait 10s for other members to join before we start loading data and pulling transactions
		try {
			Thread.sleep(10000);
		} catch(InterruptedException x){
			//
		}

		// start transaction sources
		transactionSources = new TransactionSource[transactionReaderThreads];
		for (int i = 0; i < transactionReaderThreads; ++i) {
			transactionSources[i] = new TransactionSource(transactionServerHost, transactionServerPort, hazelcast);
			transactionSources[i].start();
		}

	}

	private void configure(Properties props){
		String prop = props.getProperty(TRANSACTION_SERVER_HOST_PROP);
		if (prop == null)
			throw new RuntimeException("Required property not found: " + TRANSACTION_SERVER_HOST_PROP);
		else
			transactionServerHost = prop;

		transactionServerPort = requiredIntegerProperty(props, TRANSACTION_SERVER_PORT_PROP);
		transactionReaderThreads = requiredIntegerProperty(props, TRANSACTION_READER_THREADS_PROP);
	}

	private int requiredIntegerProperty(Properties props, String propertyName){
		int result;
		String prop = props.getProperty(propertyName);
		if (prop == null){
			throw new RuntimeException("Required property not found: " + propertyName);
		} else {
			try {
				result = Integer.parseInt(prop);
			} catch(NumberFormatException x){
				throw new RuntimeException(String.format("The %s property value (%s) could not be parsed as a number.", propertyName, prop));
			}
		}
		return result;
	}

	// main

	private static FraudDetectionServer instance;

	public static void main(String []args) {
		try {
			instance = new FraudDetectionServer();
			instance.start();
		} catch (Exception x) {
			log.severe("A fatal error occurred.", x);
			System.exit(1);
		}
	}
}
