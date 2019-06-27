package com.hazelcast.certification.domain;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class Stats implements DataSerializable {
    private int transactionsScored;

    public void registerResult(){
        transactionsScored += 1;
    }

    public int getTransactionsScored() {
        return transactionsScored;
    }

    public void accumulate(Stats stats){
        this.transactionsScored += stats.transactionsScored;
    }

    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeInt(transactionsScored);
    }

    public void readData(ObjectDataInput objectDataInput) throws IOException {
        transactionsScored = objectDataInput.readInt();
    }

    @Override
    public String toString() {
        return "Stats{" +
                "transactionsScored=" + transactionsScored +
                '}';
    }
}
