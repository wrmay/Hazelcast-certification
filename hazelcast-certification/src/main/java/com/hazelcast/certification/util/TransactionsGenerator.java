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
		String temp = properties.getProperty("TransactionsGeneratorDuration");
		if (temp == null) {
			log.info("Missing TransactionsGeneratorTestDuration. No test duration provided. Default of 2 mins will be used.");
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
                    if (key.isReadable()){
                        read(key);
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
 
    private void write(SelectionKey key) throws IOException{
        SocketChannel channel = (SocketChannel) key.channel();
        String nextTxn = getNextTransaction();

        ByteBuffer buffer = ByteBuffer.wrap(nextTxn.getBytes());
        int out = channel.write(buffer);
        while(out != SIZE_OF_PACKET){
            int tmpOut = channel.write(buffer);
            out = out + tmpOut;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
         
        socketChannel.register(selector, SelectionKey.OP_WRITE);
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        socketChannel.finishConnect();
        log.info("Connection accepted from Fraud Detection Server...");
        startTimer();
    }
 
    private void read(SelectionKey key) throws IOException{
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private int getOldNextCounter() {
        if(COUNT_TRACKER == MAX_CREDITCARD_COUNT) {
            TXNCOUNTER = new Random(1);
            COUNT_TRACKER = 0;
        }
        ++COUNT_TRACKER;
        return TXNCOUNTER.nextInt(MAX_CREDITCARD_COUNT);
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
		String creditCardNumber = txnUtil.generateCreditCardNumber(counter);
    	return txnUtil.createAndGetCreditCardTransaction(creditCardNumber, counter);
    } 
}