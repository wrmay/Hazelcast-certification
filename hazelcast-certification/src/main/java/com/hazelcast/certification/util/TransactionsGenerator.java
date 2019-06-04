package com.hazelcast.certification.util;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * Producer class of Transactions which will be consumed by Fraud Detection
 * process. This prepares comma separated String values and send them over
 * socket channel. It opens a Server socket channel on a given port and URL,
 * and waits for consumers to connect. Upon successful connection, it sends
 * transaction on the channel.
 *
 * <br>
 * There could be many consumers connected to one instance of this class and
 * each of the consumer connects on a dedicated Socket channel. This class
 * distributes writing transactions evenly across all connected channels.
 * Therefore, no 2 channels will have same transactions for Fraud Detection.
 *
 * <br>
 * One cycle generates one unique transaction for 30 million credit cards. Credit
 * cards are not chosen in sequential manner by default i.e. in each cycle, 30 million
 * credit cards are selected randomly. However, this is configurable and
 * setting <code>RandomValues</code> in <I>FraudDetection.properties</I> to <code>false</code> will
 * enable all credit cards to be selected in sequential order. After completion,
 * the cycle repeats and new unique transaction created for 30 million credit cards.
 *
 * <br>
 * The generator runs for 2 minutes by default and this duration is configurable,
 * see <code>FraudDetection.properties</code> for more details.
 *
 *
 */
public class TransactionsGenerator implements Runnable {
 
	private final static ILogger log = Logger.getLogger(TransactionsGenerator.class);

    private static String URL;
    private static int PORT;

    private final static int SIZE_OF_PACKET = 100;

    private final static long TIMEOUT = 10000;
    private int TEST_DURATION = 120;
    private boolean TEST_STARTED;
    private final static int MAX_CREDITCARD_COUNT = 30000000;

	private AtomicBoolean showStopper = new AtomicBoolean();
    private int COUNT_TRACKER;
    private boolean RANDOM_VALUES;
     
    private ServerSocketChannel serverChannel;
    private Selector selector;

    private TransactionsUtil txnUtil;
    private static Random TXNCOUNTER = new Random(1);
 
    public TransactionsGenerator(){
        init();
    }
 
    private void init(){
        loadProperties();
        txnUtil = new TransactionsUtil();

        log.info("Initializing server");
        if (selector != null) return;
        if (serverChannel != null) return;

        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(URL, PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            log.severe(e);
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
		String temp = properties.getProperty("Duration");
		if (temp == null) {
			log.info("Missing Duration. No test duration provided. Default of 2 minutes will be used.");
			return;
		}
		this.TEST_DURATION = Integer.parseInt(temp);
		
        temp = properties.getProperty("URL");
        if (temp == null) {
            log.info("Missing URL for TransactionGenerator. Provide URL to listen to incoming connections. Exiting..");
            System.exit(0);
        }
        URL = temp;

        temp = properties.getProperty("PORT");
        if (temp == null) {
            log.info("Missing Port for TransactionGenerator. Provide PORT to listen to incoming connections. Exiting..");
            System.exit(0);
        }
        PORT = Integer.parseInt(temp);

        temp = properties.getProperty("RandomValues");
        if (temp == null) {
            log.info("No value provided to enable Random generation of Credit Cards. Disabled by default.");
            RANDOM_VALUES = false;
        } else {
            RANDOM_VALUES = true;
        }
	}

	private void startTimer() {
        if(!TEST_STARTED) {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    showStopper.set(true);
                }
            }, TimeUnit.SECONDS.toMillis(TEST_DURATION));
            TEST_STARTED = true;
        }
    }
	
	private boolean shouldStop() {
        return TEST_DURATION != 0 && showStopper.get();
    }

    public void run() {
        log.info("Initialised. Now ready to accept connections...");
        try{
            while(!shouldStop()) {
                selector.select(TIMEOUT);
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()){
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()){
                        continue;
                    }
                    if (key.isAcceptable()){
                        accept(key);
                    }
                    if (key.isWritable()){
                        write(key);
                    }
                }
            }
            if(shouldStop()) {
                log.info("Test Completed. Initiating Shutdown.");
            }
        } catch (IOException e){
            log.severe(e);
		} finally{
            closeConnection();
        }
    }


    // whenever we enter this method, the buffer must have been filled.  It may be partly written to the channel.
    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        if (buffer.hasRemaining()) channel.write(buffer);

        while(!buffer.hasRemaining()){
            buffer.clear();
            String nextTxn = getNextTransaction();
            buffer.put(nextTxn.getBytes()); // really should write directly to this buffer while generating txn
            buffer.flip();
            channel.write(buffer);
        }
    }
    
    private void closeConnection(){
        log.warning("Server shutdown initiated...");
        if (selector != null){
            try {
                selector.close();
                serverChannel.socket().close();
                serverChannel.close();
                if(!serverChannel.isOpen()) {
                	log.warning("...Server shutdown complete. Exiting now.");
                }
                System.exit(0);
            } catch (IOException e) {
                log.severe(e);
            }
        }
    }

    private void accept(SelectionKey key) throws IOException{
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        ByteBuffer buffer = ByteBuffer.allocate(SIZE_OF_PACKET);
        SelectionKey keyForServerSocket = socketChannel.register(selector, SelectionKey.OP_WRITE);
        keyForServerSocket.attach(buffer);

        socketChannel.finishConnect();
        log.info("Connection accepted from Fraud Detection Server...");

        // this should be moved off to another thread or this thread will not be able to accept another
        // connection - but its OK with me right at the moment

        buffer.put(getNextTransaction().getBytes());
        buffer.flip();
        write(keyForServerSocket);

        startTimer();
    }
 
    private int getNextCounter() {
        if(COUNT_TRACKER == MAX_CREDITCARD_COUNT) {
            COUNT_TRACKER = 0;
            if(RANDOM_VALUES) {
                TXNCOUNTER = new Random(1);
            }
        }
        if(RANDOM_VALUES) {
            ++COUNT_TRACKER;
            return TXNCOUNTER.nextInt(MAX_CREDITCARD_COUNT);
        }
        return ++COUNT_TRACKER;
    }

    private String getNextTransaction() {
    	int counter = getNextCounter();
    	//if (COUNT_TRACKER % 1000 == 999) log.info("sending txn number " + (COUNT_TRACKER + 1));
		String creditCardNumber = txnUtil.generateCreditCardNumber(counter);
    	return txnUtil.createAndGetCreditCardTransaction(creditCardNumber, counter);
    } 
}