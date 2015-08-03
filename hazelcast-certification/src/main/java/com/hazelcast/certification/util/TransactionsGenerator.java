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
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
 
public class TransactionsGenerator implements Runnable {
 
	private final static ILogger log = Logger.getLogger(TransactionsGenerator.class);

    private final static String ADDRESS = "127.0.0.1";
    
    private final static int PORT = 8511;
    private final static long TIMEOUT = 10000;
    private int TEST_DURATION = 120;
    private int TRANSACTION_WRITE_INTERVAL = 500;
    
	private AtomicInteger txnCounter = new AtomicInteger();
	private AtomicBoolean showStopper = new AtomicBoolean();
     
    private ServerSocketChannel serverChannel;
    private Selector selector;
 
    public TransactionsGenerator(){
        init();
    }
 
    private void init(){
        log.info("Initializing server");
        if (selector != null) return;
        if (serverChannel != null) return;
 
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(ADDRESS, PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            log.severe(e);
        }
        loadProperties();
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
		
		temp = properties.getProperty("TransactionWriteInterval");
		if (temp == null) {
			log.info("Missing TransactionWriteInterval. To write interval provided. Default of 1ms will be used.");
			return;
		}
		this.TRANSACTION_WRITE_INTERVAL = Integer.parseInt(temp);
	}

	private void startTimer() {
    	new Timer().schedule(new TimerTask() {
    		public void run() {
    			showStopper.set(true);
    		}
    	}, TimeUnit.SECONDS.toMillis(TEST_DURATION));
    }
	
	private boolean shouldStop() {
        return TEST_DURATION != 0 && showStopper.get();
    }

	@Override
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

        nextTxn = addParityChecksum(nextTxn);

        byte[] byteBuff = nextTxn.getBytes();
        ByteBuffer outBuf = ByteBuffer.wrap(byteBuff);
        while(outBuf.hasRemaining()) {
        	channel.write(outBuf);
        }
        try {
			TimeUnit.MILLISECONDS.sleep(TRANSACTION_WRITE_INTERVAL);
		} catch (InterruptedException e) {
			log.severe(e);
		}
    }
    
    private String addParityChecksum(String rawTxn) {
        return rawTxn + "@CAFEBABE";
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
        startTimer();
    }
 
    private void read(SelectionKey key) throws IOException{
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(128);
        readBuffer.clear();
        int read;
        try {
            read = channel.read(readBuffer);
        } catch (IOException e) {
            key.cancel();
            channel.close();
            return;
        }
        if (read == -1){
            channel.close();
            key.cancel();
            return;
        }
        readBuffer.flip();
        byte[] data = new byte[128];
        readBuffer.get(data, 0, read);
        log.info("Received: " + new String(data));
 
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private int getNextCounter() {
        //TODO Add Randomness and also for multiple transactions for same credit card
        return txnCounter.incrementAndGet();
    }

    private String getNextTransaction() {
    	int counter = getNextCounter();
		String creditCardNumber = TransactionsUtil.generateCreditCardNumber(counter);
    	return TransactionsUtil.createAndGetCreditCardTransaction(creditCardNumber, counter);
    } 
}