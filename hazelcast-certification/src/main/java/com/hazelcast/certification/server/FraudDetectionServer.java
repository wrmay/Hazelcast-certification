package com.hazelcast.certification.server;

import com.hazelcast.certification.domain.CreditCardKey;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.durableexecutor.DurableExecutorService;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
	private Selector selector;
	private int PORT;
	private String URL;
	private SocketChannel channel;


	private ByteBuffer clientBuffer;
	private final static int BUFFER_SIZE = 100;
	private static CharsetDecoder decoder = Charset.forName("ASCII").newDecoder();

	private HazelcastInstance hazelcast;
	private DurableExecutorService executor;

	public FraudDetectionServer() {
		hazelcast = Hazelcast.newHazelcastInstance();
		hazelcast.getMap("transaction_history");
		executor = hazelcast.getDurableExecutorService("fraud_checker");

		loadProperties();
		clientBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	}


	private void tryChannelSocketConnection()  {
		boolean connected;
		while (!Thread.interrupted()) {
			connected = connect(new InetSocketAddress(URL, PORT));
			if (connected) {
				log.info("Connection with Transactions Generator successful...");
				return;
			}
			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e1) {
				log.severe(e1);
			}
		}
	}

	private boolean connect(InetSocketAddress address) {
		try {
			channel = SocketChannel.open();
			channel.connect(address);
			channel.write(ByteBuffer.wrap(new byte[]{0})); // this crazy thing fixes a problem with read never returning.
			channel.configureBlocking(false);
			return true;
		} catch (Exception e) {
			if(channel != null) {
				try {
					channel.close();
				} catch (IOException e1) {
					log.severe(e1);
				}
			}
			log.warning("Remote node TransactionGenerator not available. Retry in 3 seconds...");
		}
		return false;
	}

	private void run() {
		try {
			tryChannelSocketConnection();
			selector = Selector.open();
			channel.register(selector, SelectionKey.OP_READ);
			read();
			while (!Thread.interrupted()) {
				selector.select();
				Iterator<SelectionKey> keys = selector.selectedKeys()
						.iterator();

				while (keys.hasNext()) {
					SelectionKey key = keys.next();
					keys.remove();

					if (!key.isValid())
						continue;

					if (key.isReadable()) {
						read();
					}
				}
			}
		} catch (Exception e) {
			log.severe(e);
		} finally {
			close();
		}
	}

	private void close() {
		try {
			selector.close();
			hazelcast.shutdown();
		} catch (IOException e) {
			log.severe(e);
		}
	}

	private void read() throws IOException {
		String txnString;
		channel.read(clientBuffer);
		if (clientBuffer.position() == clientBuffer.capacity()){
			clientBuffer.flip();
			txnString = decoder.decode(clientBuffer).toString();
			clientBuffer.clear();
			process(txnString);
		}
	}

	// should we farm this out to a thread pool ??
	private void process(String rawTxnString) {
		Transaction t = prepareTransaction(rawTxnString.substring(0, rawTxnString.length() - 9));
		TransactionScoringTask task = new TransactionScoringTask(t);
		executor.submitToKeyOwner(task, new CreditCardKey(t.getCreditCardNumber()));
	}

	private Transaction prepareTransaction(String txnString) throws RuntimeException {
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

	}


	public static void main(String []args) {
		FraudDetectionServer server = new FraudDetectionServer();
		log.info("Waiting 10s for other cluster members to join.");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException x){
			//
		}

		// start processing transactions.
		server.run();
	}
}
