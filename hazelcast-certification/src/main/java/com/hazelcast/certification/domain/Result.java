package com.hazelcast.certification.domain;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

/**
 * Created by rahul on 07/08/15.
 */
public class Result implements DataSerializable {

    private boolean fraudTransaction;
    private String creditCardNumber;

    public Result() {}

    public Result( String creditCardNumber, boolean fraudTransaction) {
        this.fraudTransaction = fraudTransaction;
        this.creditCardNumber = creditCardNumber;
    }

    public void setFraudTransaction(boolean isFraudTransaction) {
        this.fraudTransaction = isFraudTransaction;
    }

    public boolean isFraudTransaction() {
        return fraudTransaction;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }


    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeUTF(creditCardNumber);
        objectDataOutput.writeBoolean(fraudTransaction);
    }

    public void readData(ObjectDataInput objectDataInput) throws IOException {
        creditCardNumber = objectDataInput.readUTF();
        fraudTransaction = objectDataInput.readBoolean();
    }
}


