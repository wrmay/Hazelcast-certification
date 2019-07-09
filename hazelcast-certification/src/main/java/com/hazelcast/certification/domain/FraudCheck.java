package com.hazelcast.certification.domain;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class FraudCheck implements DataSerializable {
    private long timestamp;
    private boolean isFraudulent;
    private int failedOn;

    public FraudCheck(){
        // need this for deserialization
    }

    public FraudCheck(boolean isFraud, int failedOn){
        this.timestamp = System.currentTimeMillis();
        this.isFraudulent = isFraud;
        this.failedOn = failedOn;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getFailedOn(){ return failedOn;}

    public boolean isFraudulent() {
        return isFraudulent;
    }

    public void setFraudulent(boolean fraudulent) {
        isFraudulent = fraudulent;
        timestamp = System.currentTimeMillis();
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeLong(timestamp);
        objectDataOutput.writeBoolean(isFraudulent);
        objectDataOutput.writeInt(failedOn);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        this.timestamp = objectDataInput.readLong();
        this.isFraudulent = objectDataInput.readBoolean();
        this.failedOn = objectDataInput.readInt();
    }
}
