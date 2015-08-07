package com.hazelcast.certification.server;

import com.hazelcast.certification.process.FraudDetection;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class FraudDetectionServer {

	private final static ILogger log = Logger.getLogger(FraudDetectionServer.class);
	private Selector selector;
	private int PORT;
	private String URL;
	private BlockingQueue<String> txnQueue;
	private int queueCapacity;
	private SocketChannel channel;

	private String FRAUD_DETECTION_IMPL_PROVIDER;
	private final static int DEFAULT_QUEUE_CAPACITY = 10000;

	private ByteBuffer clientBuffer;
	private final static int BUFFER_SIZE = 100;
	private static CharsetDecoder decoder = Charset.forName("ASCII").newDecoder();

	public FraudDetectionServer() {
		setup();
		loadProperties();
		bindQueue();
		initializeFraudDetection();
	}

	private void setup() {
		clientBuffer = ByteBuffer.allocate(100);
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
			log.severe("Invalid FraudDetection implementation provided. The implementation must extend FraudDetection. Exiting...");
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

	private void tryChannelSocketConnection() throws IOException {
		boolean connected;
		while (!Thread.interrupted()) {
			connected = connect(new InetSocketAddress(URL, PORT));
			if (connected) {
				log.info("Connections Successful...");
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
			Socket socket = channel.socket();
			socket.setKeepAlive(true);
			socket.setReuseAddress(true);
			channel.socket().connect(address);
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
			while (!Thread.interrupted()) {
				selector.selectNow();
				Iterator<SelectionKey> keys = selector.selectedKeys()
						.iterator();

				while (keys.hasNext()) {
					SelectionKey key = keys.next();
					keys.remove();

					if (!key.isValid())
						continue;

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
		clientBuffer.clear();
		SocketChannel channel = (SocketChannel) key.channel();
		int length = channel.read(clientBuffer);

		if (length > 0) {
			channel.register(selector, SelectionKey.OP_WRITE);
			int healthCheckDegree = 0;
			while (length != BUFFER_SIZE) {
				healthCheckDegree++;

				int tmpLength = channel.read(clientBuffer);
				length = length + tmpLength;

				if (healthCheckDegree >= 200) {
					log.info("Bad Network. Waiting for transactions.");
					try {
						Thread.sleep(10);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			clientBuffer.flip();
			CharBuffer charBuffer = decoder.decode(clientBuffer);
			//if(length == BUFFER_SIZE) {
				process(charBuffer.toString());
			//}
			clientBuffer.clear();
		} else {
			handleRemoteSocketTermination();
		}
	}


	private void handleRemoteSocketTermination() {
		try {
			channel.close();
			log.warning("Shutdown complete.");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void write(SelectionKey key) throws IOException {
		key.interestOps(SelectionKey.OP_READ);
	}

	private void process(String rawTxnString) {
		rawTxnString = rawTxnString.substring(0, rawTxnString.length() - 9);
		txnQueue.offer(rawTxnString);
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
		String temp = properties.getProperty("FraudDetectionImplementation");
		if (temp == null) {
			log.severe("Missing FraudDetectionImplementation. No implementation provided for FraudDetection. Exiting...");
			System.exit(0);
		}
		this.FRAUD_DETECTION_IMPL_PROVIDER = temp;

		temp = properties.getProperty("PORT");
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

		temp = properties.getProperty("QueueCapacity");
		if (temp == null) {
			log.warning("Missing QueueCapacity. Using default of "
					+ DEFAULT_QUEUE_CAPACITY);
			queueCapacity = DEFAULT_QUEUE_CAPACITY;
		} else {
			queueCapacity = Integer.parseInt(temp);
		}

		temp = properties.getProperty("ExecutorPoolSize");
		if (temp == null) {
			log.warning("Missing ExecutorPoolSize. Using default of 2");
			System.setProperty("ExecutorPoolSize", String.valueOf(2));
		} else {
			System.setProperty("ExecutorPoolSize", temp);
		}

		temp = properties.getProperty("ShowCacheStatistics");
		if (temp == null) {
			log.warning("Default ShowCacheStatistics used");
			temp = "false";
		}
		System.setProperty("ShowCacheStatistics", temp);
	}

	private void bindQueue() {
		txnQueue = TransactionQueue.getTransactionQueue(queueCapacity);
	}

	public static void main(String args[]) {
		new FraudDetectionServer().run();
	}
}
