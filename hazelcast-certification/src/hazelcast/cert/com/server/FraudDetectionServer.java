package hazelcast.cert.com.server;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import hazelcast.cert.com.process.FraudDetection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

public class FraudDetectionServer {

	private final static ILogger log = Logger.getLogger(FraudDetectionServer.class);
	private Selector selector;
	private String CHECKSUM = "@CAFEBABE";
	private int PORT;
	private String URL;
	private BlockingQueue<String> txnQueue;
	private int queueCapacity;
	private String FRAUD_DETECTION_IMPL_PROVIDER;

	private final static int DEFAUT_QUEUE_CAPACITY = 10000;

	public FraudDetectionServer() {
		loadProperties();
		bindQueue();
		initializeFraudDetection();
	}

	private void initializeFraudDetection() {
		Object fraudDetectionImpl = null;
		try {
			fraudDetectionImpl = Class.forName(FRAUD_DETECTION_IMPL_PROVIDER)
					.newInstance();
		} catch (InstantiationException e) {
			log.severe("Error Initializing FraudDetectionImpl", e);
		} catch (IllegalAccessException e) {
			log.severe(
					"Can not access definition of implementation of FraudDetection",
					e);
		} catch (ClassNotFoundException e) {
			log.severe("Can not locate implementation of FraudDetection", e);
		}
		if (fraudDetectionImpl == null
				|| !(fraudDetectionImpl instanceof FraudDetection)) {
			log.severe("Invalid FraudDetection implementation provided. The implementation must extend hazelcast.cert.com.process.FraudDetection. Exiting...");
			System.exit(0);
		}
		final FraudDetection fraudD = (FraudDetection) fraudDetectionImpl;
		fraudD.bindTransactionQueue(txnQueue);
		new Thread() {
			public void run() {
				fraudD.run();
			}
		}.start();
	}

	private void run() {
		SocketChannel channel;
		try {
			selector = Selector.open();
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			channel.register(selector, SelectionKey.OP_CONNECT);

			channel.connect(new InetSocketAddress(URL, PORT));

			log.info("Connecting to TransactionsGenerator at " + URL + "@"
					+ PORT);

			while (!Thread.interrupted()) {
				selector.select(10000);
				Iterator<SelectionKey> keys = selector.selectedKeys()
						.iterator();

				while (keys.hasNext()) {
					SelectionKey key = keys.next();
					keys.remove();

					if (!key.isValid())
						continue;

					if (key.isConnectable()) {
						connect(key);
					}
					if (key.isWritable()) {
						write(key);
					}
					if (key.isReadable()) {
						read(key);
					}
				}
			}
		} catch (IOException e) {
			log.severe(e);
		} finally {
			close();
		}
	}

	private void close() {
		try {
			selector.close();
		} catch (IOException e) {
			log.severe(e);
		}
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer readBuffer = ByteBuffer.allocate(128);
		int length = 0;
		try {
			length = channel.read(readBuffer);
		} catch (IOException e) {
			log.severe("Reading problem, closing connection");
			key.cancel();
			channel.close();
			return;
		}
		readBuffer.flip();

		byte[] buff = new byte[length];
		readBuffer.get(buff, 0, length);
		readBuffer.clear();
		String txnString = new String(buff);
		if (error(txnString)) {
			log.warning("Bad Transaction Received. Discarding...");
		} else
			process(txnString);
	}

	private boolean error(String txnString) {
		if (txnString.endsWith(CHECKSUM)) {
			txnString = txnString.substring(0, txnString.length() - 9);
			return false;
		}
		return true;
	}

	private void write(SelectionKey key) throws IOException {
		key.interestOps(SelectionKey.OP_READ);
	}

	private void connect(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		channel.configureBlocking(false);
		if (channel.isConnectionPending()) {
			channel.finishConnect();
		}
		// while (!channel.isConnected()) {
		// try {
		// log.warning("TransactionGenerator not available. Waiting...");
		// channel.finishConnect();
		// Thread.sleep(3000);
		// // } catch(ConnectException e) {
		// // } catch(ClosedChannelException e) {
		// // log.warning("TransactionGenerator not available. Waiting...");
		// } catch(InterruptedException e) {
		// log.severe(e);
		// }
		// }
		log.info("Connection with TransactionGenerator successful.!!");
		channel.register(selector, SelectionKey.OP_WRITE);
	}

	private void process(String rawTxnString) {
		txnQueue.offer(rawTxnString);
	}

	public void setTransactionQueue(BlockingQueue<String> queue) {
		this.txnQueue = queue;
	}

	private void loadProperties() {
		String propFileName = "config.properties";
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
		String temp = properties.getProperty("FraudDetectionImplementation");
		if (temp == null) {
			log.severe("Missing FraudDetectionImplementation. No implementation provided for FraudDetection. Exiting...");
			System.exit(0);
		}
		this.FRAUD_DETECTION_IMPL_PROVIDER = temp;

		temp = properties.getProperty("Port");
		if (temp == null) {
			log.severe("Missing Port. No Port provided for socket communication for incoming transactions. Exiting...");
			System.exit(0);
		}
		this.PORT = Integer.parseInt(temp);

		temp = properties.getProperty("URL");
		if (temp == null) {
			log.severe("Missing URL. No URL provided for socket communication for incoming transactions. Exiting...");
			System.exit(0);
		}
		this.URL = temp;

		temp = properties.getProperty("QueueCapacity");
		if (temp == null) {
			log.warning("Missing QueueCapacity. Using default of "
					+ DEFAUT_QUEUE_CAPACITY);
			queueCapacity = DEFAUT_QUEUE_CAPACITY;
		} else {
			queueCapacity = Integer.parseInt(temp);
		}

		temp = properties.getProperty("DoWarmup");
		Boolean doWarmup;
		if (temp == null) {
			log.info("Missing DoWarmup. No configuration provided for initial warmup");
			doWarmup = false;
		} else {
			doWarmup = Boolean.parseBoolean(properties.getProperty("doWarmup"));
		}
		System.setProperty("DoWarmup", doWarmup.toString());

		temp = properties.getProperty("ShowCacheStatistics");
		if (temp == null) {
			log.warning("Default ShowCacheStatistics used");
		}
		System.setProperty("ShowCacheStatistics", temp);
		
		temp = properties.getProperty("DataAccessManager");
		if (temp == null) {
			log.severe("Missing DataAccessManager. Exiting...");
			System.exit(0);
		}
		System.setProperty("DataAccessManager", temp);
	}

	private void bindQueue() {
		txnQueue = TransactionQueue.getTransactionQueue(queueCapacity);
	}

	public static void main(String args[]) {
		new FraudDetectionServer().run();
	}
}
