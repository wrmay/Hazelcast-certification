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
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class FraudDetectionClient extends Thread {

    private static ILogger log = Logger.getLogger(FraudDetectionClient.class);

    private static final String CREDIT_CARD_COUNT_PROP = "transaction.client.card.count";
    private static final String TRANSACTION_CLIENT_THREADS_PROP = "transaction.client.threads";
    private static final String TRANSACTION_CLIENT_REQUESTS_IN_FLIGHT = "transaction.client.max_outstanding_requests_per_thread";

    private static final Counter gets = Counter.build().name("get_successes_total").help("total successful gets").register();
    private static final Counter misses = Counter.build().name("get_misses_total").help("total gets returning null").register();
    private static final Counter exceptions = Counter.build().name("get_exceptions_total").help("total gets throwing an exception").register();
    private static final Gauge latency = Gauge.build().name("get_latency").help("get latency in ms").register();



    private LinkedBlockingQueue<Request> inFlightRequests;
    IMap<String, TransactionHistoryContainer> txnHistory;
    private HazelcastInstance hz;
    private int maxOutstandingRequests ;
    private TransactionsUtil transactionGenerator;
    private int cardCount;
    private Random random;


    public void run() {
        long start = System.currentTimeMillis();
        long elapsed = 0;
        int count = 0;
        while (true) {

            int seq = random.nextInt(cardCount);
            String ccNum = transactionGenerator.generateCreditCardNumber(seq);
            try {
                TransactionHistoryContainer result = txnHistory.get(ccNum);
                if (result == null) {
                    misses.inc();
                }
                else {
                    ++count;
                    gets.inc();
                }
            } catch(Exception x){
                exceptions.inc();
            }

            if (count % 100 == 0){
                long now = System.currentTimeMillis();
                elapsed = now  - start;
                latency.set( ((double) elapsed) / 100.0  );
                start = now;
                elapsed = 0;
            }

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
