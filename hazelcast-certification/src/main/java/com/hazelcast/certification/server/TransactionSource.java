package com.hazelcast.certification.server;

import com.hazelcast.certification.domain.CreditCardKey;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.durableexecutor.DurableExecutorService;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransactionSource extends Thread {

    private ILogger log = Logger.getLogger(TransactionSource.class);

    private final static int BUFFER_SIZE = 100;
    public static final String TRANSACTION_SOURCE_ON_PARAMETER = "transaction_source_on";

    private byte []buffer;
    private int bytesRead;
    private AtomicBoolean running;
    private Socket sock;
    private InputStream in;
    private Charset encoding;
    private IMap<CreditCardKey, LinkedList<Transaction>> txnHistory;
    private IMap<String, Boolean> controller;

    public TransactionSource(String url, int port, HazelcastInstance hz) throws IOException {
        buffer = new byte [BUFFER_SIZE];
        sock = new Socket(url, port);
        in = sock.getInputStream();
        running = new AtomicBoolean(true);
        encoding = Charset.forName("ASCII");
        txnHistory = hz.getMap("transaction_history");  // if the data is not already loaded, this will potentially take a long time!
        controller = hz.getMap("controller");
        controller.set(TRANSACTION_SOURCE_ON_PARAMETER, Boolean.FALSE);
        this.setDaemon(true);
    }

    /*
     * blocks up to 10s waiting for shutdown
     */
    public void shutdown() throws IOException {
        running.set(false);
        this.interrupt();
        try {
            this.join(10000);
        } catch(InterruptedException x){
            log.warning("Could not verify shutdown of TransactionSource thread");
        }
        sock.close();
    }

    public void run(){
        boolean running;
        while(true){
            running = controller.get(TRANSACTION_SOURCE_ON_PARAMETER);
            if (running){
                try {
                    bytesRead = bytesRead + in.read(buffer,bytesRead, buffer.length - bytesRead);
                    if (bytesRead == buffer.length){
                        bytesRead = 0;  // better to put it before process() in case it throws an exception
                        process();
                    }
                } catch(IOException x){
                    log.severe("An exception occurred while attempting to read transactions.", x);
                }
            } else {
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException x){
                    // no problem
                }
            }
        }
    }

    private void process() {
        String rawTxnString = new String(buffer, encoding);
        String txnString = rawTxnString.substring(0, rawTxnString.length() - 9);
        int i = txnString.indexOf(",");
        String ccNumber = txnString.substring(0, i);
        txnHistory.executeOnKey(new CreditCardKey(ccNumber), new ProcessTransactionEntryProcessor(txnString));
    }

//    private Transaction prepareTransaction(String txnString) throws RuntimeException {
//        Transaction txn = new Transaction();
//        String[] cName = txnString.split(",");
//        txn.setCreditCardNumber(cName[0]);
//        txn.setTimeStamp(Long.parseLong(cName[1]));
//        txn.setCountryCode(cName[2]);
//        txn.setResponseCode(cName[3]);
//        txn.setTxnAmt(cName[4]);
//        txn.setMerchantType(cName[6]);
//        txn.setTxnCity(cName[7]);
//        txn.setTxnCode(cName[8]);
//        return txn;
//    }
}
