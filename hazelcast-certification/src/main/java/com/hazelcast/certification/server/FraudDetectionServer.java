package com.hazelcast.certification.server;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;
import java.util.Properties;

/**
 *
 */
public class FraudDetectionServer {

	private final static ILogger log = Logger.getLogger(FraudDetectionServer.class);


	// state
	private HazelcastInstance hazelcast;

	public void start() throws IOException {
		hazelcast = Hazelcast.newHazelcastInstance();
	}


	// main

	private static FraudDetectionServer instance;

	public static void main(String []args) {
		try {
			// start Prometheus exporter
			//DefaultExports.initialize();
			HTTPServer server = new HTTPServer(7777);

			instance = new FraudDetectionServer();
			instance.start();
		} catch (Exception x) {
			log.severe("A fatal error occurred.", x);
			System.exit(1);
		}
	}
}
