package com.hazelcast.certification.server;

import com.hazelcast.certification.domain.CreditCardKey;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.durableexecutor.DurableExecutorService;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * This is just used to test the txn generator
 */
public class TransactionSource extends Thread {

    private ILogger log = Logger.getLogger(TransactionSource.class);

    private final static int BUFFER_SIZE = 100;

    private byte []buffer;
    private int bytesRead;
    private DurableExecutorService executor;
    private AtomicBoolean running;
    private Socket sock;
    private InputStream in;
    private Charset encoding;

    public TransactionSource(String url, int port, DurableExecutorService executor) throws IOException {
        this.executor = executor;

        buffer = new byte [BUFFER_SIZE];
        sock = new Socket(url, port);
        in = sock.getInputStream();
        running = new AtomicBoolean(true);
        encoding = Charset.forName("ASCII");
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
        while(running.get()){
            try {
                bytesRead = bytesRead + in.read(buffer,bytesRead, buffer.length - bytesRead);
                if (bytesRead == buffer.length){
                    process();
                    bytesRead = 0;
                }
            } catch(IOException x){
                log.severe("An exception occurred while attempting to read transactions.", x);
            }
        }
    }

    private void process() {
        String rawTxnString = new String(buffer, encoding);
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
}
