package com.hazelcast.certification.domain;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import java.io.IOException;

public class TransactionHistoryNode implements IdentifiedDataSerializable {

    public TransactionHistoryNode() {

    }

    public TransactionHistoryNode(Transaction t) {
        this.transaction = t;
    }

    public Transaction transaction;
    public TransactionHistoryNode previous;  // previous in the sense of earlier

    @Override
    public int getFactoryId() {
        return TransactionDataSerializableFactory.FACTORY_ID;
    }

    @Override
    public int getId() {
        return TransactionDataSerializableFactory.TRANSACTION_NODE_TYPE;
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeObject(transaction);
        objectDataOutput.writeObject(previous);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        transaction = objectDataInput.readObject();
        previous = objectDataInput.readObject();
    }
}
