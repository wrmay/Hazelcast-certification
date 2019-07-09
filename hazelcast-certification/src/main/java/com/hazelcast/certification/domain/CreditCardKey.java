package com.hazelcast.certification.domain;

import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class CreditCardKey implements PartitionAware<String> , DataSerializable {

    private String ccNumber;

    public CreditCardKey(){};

    public CreditCardKey(String ccNumber){
        this.ccNumber = ccNumber;
    }

    public String getPartitionKey() {
        return getPartition(ccNumber);
    }

    public static String getPartition(String ccNumber) { return ccNumber.substring(ccNumber.length() - 2);}

    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeUTF(ccNumber);
    }

    public void readData(ObjectDataInput objectDataInput) throws IOException {
        ccNumber = objectDataInput.readUTF();
    }

    public String getCardNumber(){ return ccNumber;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CreditCardKey that = (CreditCardKey) o;

        return ccNumber.equals(that.ccNumber);
    }

    @Override
    public int hashCode() {
        return ccNumber.hashCode();
    }
}
