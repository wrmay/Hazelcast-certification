package com.hazelcast.certification.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TransactionQueue {

	private static int queueCapacity;
	
	private static class Instance {
		static final BlockingQueue<String> queue = new LinkedBlockingQueue<String>(queueCapacity);
	}
	
	public static BlockingQueue<String> getTransactionQueue(int capacity) {
		queueCapacity = capacity;
		return Instance.queue;
	}
}
