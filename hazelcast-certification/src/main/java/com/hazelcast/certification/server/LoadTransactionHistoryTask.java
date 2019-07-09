package com.hazelcast.certification.server;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;

public class LoadTransactionHistoryTask implements Runnable, HazelcastInstanceAware, Serializable {

    private transient HazelcastInstance hz;

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hz = hazelcastInstance;
    }

    @Override
    public void run() {
        hz.getMap("transaction_history");
    }
}
