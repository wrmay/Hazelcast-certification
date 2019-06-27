package com.hazelcast.certification.domain;

import com.hazelcast.core.PartitionAware;

public class CreditCardKey implements PartitionAware<String> {

    private String ccNumber;

    public CreditCardKey(String ccNumber){
        this.ccNumber = ccNumber;
    }

    public String getPartitionKey() {
        return ccNumber.substring(ccNumber.length() - 2);
    }
}
