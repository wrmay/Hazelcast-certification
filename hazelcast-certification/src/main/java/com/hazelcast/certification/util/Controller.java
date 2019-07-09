package com.hazelcast.certification.util;

import com.hazelcast.certification.server.FraudDetectionServer;
import com.hazelcast.certification.server.LoadTransactionHistoryTask;
import com.hazelcast.certification.server.TransactionSource;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;

import java.util.concurrent.ExecutorService;

public class Controller {
    public static void main(String []args){
        String hazelcastConfigPath = System.getProperty("hazelcast.client.config");
        if (hazelcastConfigPath == null){
            System.out.println("Please configure the hazelcast client by setting the \"hazelcast.client.config\" java system property");
            System.exit(1);
        }

        if (args.length == 0){
            showHelp();
            System.exit(1);
        }

        String command = args[0];
        if (command.equals("help")){
            showHelp();
            System.exit(0);
        }

        String target = "";
        if (args.length > 1){
            target = args[1];
        }

        int returncode = 0;
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();

        try {
            if (command.equals("load")) {
                IExecutorService executor = hz.getExecutorService("default");
                executor.submit(new LoadTransactionHistoryTask());
                System.out.println("load started");
            } else if (command.equals("start") ) {
                IMap<String, Boolean> controller = hz.getMap("controller");
                controller.set(TransactionSource.TRANSACTION_SOURCE_ON_PARAMETER, Boolean.TRUE);
                System.out.println("transaction processing started");
            } else if (command.equals("stop")) {
                IMap<String, Boolean> controller = hz.getMap("controller");
                controller.set(TransactionSource.TRANSACTION_SOURCE_ON_PARAMETER, Boolean.FALSE);
                System.out.println("transaction processing stopped");
            }

            System.out.println("Unrecognized command: " + command);
            returncode = 1;
        } catch(Exception x){
            returncode = 1;
            x.printStackTrace(System.out);
        }
        finally {
            hz.shutdown();
        }

        System.exit(returncode);
    }

    private static void showHelp(){
        System.out.println("Usage: java -cp <classpath> -Dhazelcast.client.config=<pth/to/hazelcast-client.xml> com.hazelcast.certification.util.Controller <command>");
        System.out.println();
        System.out.println("command can be: ");
        System.out.println("\tload");
        System.out.println("\tstart processing");
        System.out.println("\tstop processing");
        System.out.println("\treport throughput");
        System.out.println("\thelp");
    }
}
