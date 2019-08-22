package com.hazelcast.certification.server;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class Server {
    public static void main(String []args){
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<Object, Object> map = hz.getMap("transaction_history");
        System.out.println(String.format("transaction_history map size: %d", map.size()));
    }
}
