package com.hazelcast.certification.domain;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class Stats implements DataSerializable {
    private int transactionsScored;
    private int fraudulentTransactions;

    public int getTransactionsScored() {
        return transactionsScored;
    }
    public int getFraudulentTransactions(){ return fraudulentTransactions;}

    public void accumulate(boolean isFraudulent){
        this.transactionsScored += 1;
        if (isFraudulent) this.fraudulentTransactions += 1;
    }

    public void combine(Stats stats){
        this.transactionsScored += stats.transactionsScored;
        this.fraudulentTransactions += stats.fraudulentTransactions;
    }

    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeInt(transactionsScored);
        objectDataOutput.writeInt(fraudulentTransactions);
    }

    public void readData(ObjectDataInput objectDataInput) throws IOException {
        transactionsScored = objectDataInput.readInt();
        fraudulentTransactions = objectDataInput.readInt();
    }

    @Override
    public String toString() {
        return String.format("Total (Fraudulent) Transactions: %d (%d)",transactionsScored, fraudulentTransactions);
    }
}
