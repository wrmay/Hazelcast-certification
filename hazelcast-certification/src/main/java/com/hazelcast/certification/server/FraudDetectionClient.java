package com.hazelcast.certification.server;

import com.hazelcast.certification.domain.TransactionHistoryContainer;
import com.hazelcast.certification.util.TransactionsUtil;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class FraudDetectionClient extends Thread {

    private static ILogger log = Logger.getLogger(FraudDetectionClient.class);

    private static final String CREDIT_CARD_COUNT_PROP = "transaction.client.card.count";
    private static final String TRANSACTION_CLIENT_THREADS_PROP = "transaction.client.threads";
    private static final String TRANSACTION_CLIENT_REQUESTS_IN_FLIGHT = "transaction.client.max_outstanding_requests_per_thread";

    private static final Counter transactionsSubmitted = Counter.build().name("transactions_submitted_total").help("total transactions submitted").register();
    private static final Counter transactionsSucceeded = Counter.build().name("transactions_succeeded_total").help("total transactions that were submitted and a response was returned").register();
    private static final Counter transactionsFailed = Counter.build().name("transactions_failed_total").help("total transactions that were submitted but failed").register();
    private static final Counter transactionsSlow = Counter.build().name("transactions_slow_total").help("total transactions that returned in more than 1 second").register();


    private LinkedBlockingQueue<Request> inFlightRequests;
    IMap<String, TransactionHistoryContainer> txnHistory;
    private HazelcastInstance hz;
    private int maxOutstandingRequests ;
    private TransactionsUtil transactionGenerator;
    private int cardCount;
    private Random random;


    public void run() {
        while (true) {
            int seq = random.nextInt(cardCount);
            String ccNum = transactionGenerator.generateCreditCardNumber(seq);
            String transactionString = transactionGenerator.createAndGetCreditCardTransaction(ccNum, seq, true);

            Request request = new Request(transactionString);
            try {
                inFlightRequests.put(request);  // blocks for back pressure
            } catch (InterruptedException ix) {
                log.warning("Unexpected exception.");
            }

            txnHistory.submitToKey(ccNum, new ProcessTransactionEntryProcessor(transactionString), new TransactionCallback(request));
            transactionsSubmitted.inc();
        }
    }


    private class TransactionCallback implements ExecutionCallback<Object> {

        private Request request;

        public TransactionCallback(Request request) {
            this.request = request;
        }

        @Override
        public void onResponse(Object o) {
            inFlightRequests.remove(request);
            transactionsSucceeded.inc();
            long elapsed = System.currentTimeMillis() - request.startTime;
            if (elapsed > 1000)
                transactionsSlow.inc();
        }

        @Override
        public void onFailure(Throwable throwable) {
            inFlightRequests.remove(request);
            transactionsFailed.inc();
        }
    }


    public FraudDetectionClient(HazelcastInstance hz, int maxOutstandingRequests, int cardCount) {
        this.hz = hz;
        this.maxOutstandingRequests = maxOutstandingRequests;
        this.inFlightRequests = new LinkedBlockingQueue<>(maxOutstandingRequests);
        this.txnHistory = hz.getMap("transaction_history");
        this.transactionGenerator = new TransactionsUtil();
        this.random = new Random();
        this.cardCount = cardCount;
        this.setDaemon(true);
    }

    public static void main(String[] args) {
        try {
            HTTPServer server = new HTTPServer(7777);

            HazelcastInstance hz = HazelcastClient.newHazelcastClient();

            int threads = getIntegerPropertyOrDefault(System.getProperties(), TRANSACTION_CLIENT_THREADS_PROP, 8);
            int maxInFlightRequests = getIntegerPropertyOrDefault(System.getProperties(), TRANSACTION_CLIENT_REQUESTS_IN_FLIGHT, 200);
            int cardCount = requiredIntegerProperty(System.getProperties(),CREDIT_CARD_COUNT_PROP);

            FraudDetectionClient[] clients = new FraudDetectionClient[threads];
            for (int i = 0; i < threads; ++i) {
                clients[i] = new FraudDetectionClient(hz, maxInFlightRequests,cardCount);
                clients[i].start();
            }


        } catch (Exception x) {
            log.severe("A fatal error occurred.", x);
            System.exit(1);
        }
    }

    private static int requiredIntegerProperty(Properties props, String propertyName) {
        int result;
        String prop = props.getProperty(propertyName);
        if (prop == null) {
            throw new RuntimeException("Required property not found: " + propertyName);
        } else {
            try {
                result = Integer.parseInt(prop);
            } catch (NumberFormatException x) {
                throw new RuntimeException(String.format("The %s property value (%s) could not be parsed as a number.", propertyName, prop));
            }
        }
        return result;
    }

    private static int getIntegerPropertyOrDefault(Properties props, String propertyName, int defaultVal) {
        int result;
        String prop = props.getProperty(propertyName);
        if (prop == null) {
            return defaultVal;
        } else {
            try {
                result = Integer.parseInt(prop);
            } catch (NumberFormatException x) {
                throw new RuntimeException(String.format("The %s property value (%s) could not be parsed as a number.", propertyName, prop));
            }
        }
        return result;
    }


    private static class Request {
        public Request(String cardTransaction) {
            this.cardTransaction = cardTransaction;
            this.startTime = System.currentTimeMillis();
        }

        public String cardTransaction;
        public long startTime;
    }

}
