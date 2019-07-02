package com.hazelcast.certification.server;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.durableexecutor.DurableExecutorService;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>
 * The main class that starts Fraud Detection process.
 *
 * Loads all properties in the system. Each instance of this class makes a socket connection
 * with <code>TransactionsGenerator</code> running on the given URL and Port. If no instance
 * found on provided URL and Port, it waits for 3 seconds and tries again, in a loop. After
 * successful connection, it starts receiving transactions in the form of comma separated
 * String and puts them on a <i><code>java.util.concurrent.BlockingQueue</code></i> to be consumed by
 * the implementation of <code>com.hazelcast.certification.process.FraudDetection</code>.
 * Size of this queue is configurable, see <code>FraudDetection.properties</code> for more details.
 *
 * <br>
 *
 * After completion of the process, it closes all open socket connections, flushes streams and
 * closes them and initiates graceful shutdown. At the very end, it also prints the overall
 * application throughput on console.
 *
 */
public class FraudDetectionServer {

	private final static ILogger log = Logger.getLogger(FraudDetectionServer.class);

	private int PORT;
	private String URL;
	private int TXN_SOURCE_THREAD_COUNT = 4;

	private HazelcastInstance hazelcast;
	private DurableExecutorService executor;
	private TransactionSource []transactionSources;

	public FraudDetectionServer() {
		hazelcast = Hazelcast.newHazelcastInstance();
		hazelcast.getMap("transaction_history");
		executor = hazelcast.getDurableExecutorService("fraud_checker");

		loadProperties();
	}

	private void startTransactionSources() throws IOException {
		transactionSources = new TransactionSource[TXN_SOURCE_THREAD_COUNT];
		for(int i=0;i< TXN_SOURCE_THREAD_COUNT; ++i) {
			transactionSources[i] = new TransactionSource(URL,PORT,executor);
			transactionSources[i].start();
		}

	}

	private void loadProperties() {
		String propFileName = "FraudDetection.properties";
		InputStream stream = getClass().getClassLoader().getResourceAsStream(
				propFileName);
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

		String temp = properties.getProperty("PORT");
		if (temp == null) {
			log.severe("Missing Port. No Port provided for socket communication for incoming transactions. Exiting...");
			System.exit(0);
		}
		this.PORT = Integer.parseInt(temp);

		temp = properties.getProperty("URL");
		if (temp == null) {
			log.severe("Missing URL. No URL provided for socket communication for TransactionGenerator. Exiting...");
			System.exit(0);
		}
		this.URL = temp;

		temp = properties.getProperty("transactionSourceThreadCount");
		if (temp != null){
			this.TXN_SOURCE_THREAD_COUNT = Integer.parseInt(temp);
		}

	}


	public static void main(String []args) {
		try {
			FraudDetectionServer server = new FraudDetectionServer();
			log.info("Waiting 10s for other cluster members to join.");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException x) {
				//
			}

			server.startTransactionSources();
		} catch(Exception x){
			log.severe(x);
			System.exit(1);
		}
	}
}
