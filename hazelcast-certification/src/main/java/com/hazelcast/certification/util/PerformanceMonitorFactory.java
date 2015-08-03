package com.hazelcast.certification.util;

import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceMonitorFactory {

	private static volatile AtomicInteger tps;
	
	public static AtomicInteger getTPSHandler() {
		if(tps == null) {
			tps = new AtomicInteger();
		}
		return tps;
	}
}
