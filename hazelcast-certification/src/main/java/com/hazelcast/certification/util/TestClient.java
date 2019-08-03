package com.hazelcast.certification.util;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class TestClient {

    private static int PUT_COUNT = 10000;

    public static void main(String []args){
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        IMap<Integer, String> testMap = hz.getMap("test");
        long start = System.currentTimeMillis();
        for (int i = 0; i < PUT_COUNT; ++i) testMap.put(Integer.valueOf(i),"hello");
        long elapsed = System.currentTimeMillis()  - start;

        double rtt = (double) elapsed / (double) PUT_COUNT;
        System.out.println(String.format("Put %d entries in %dms.  Estimated RTT is %fms.", PUT_COUNT, elapsed, rtt));

        hz.shutdown();
    }
}
