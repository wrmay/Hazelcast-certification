package com.hazelcast.certification.domain;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class TransactionDataSerializableFactory  implements DataSerializableFactory {

    public static final int FACTORY_ID = 1;

    public static final int TRANSACTION_TYPE = 1;
    public static final int TRANSACTION_NODE_TYPE = 2;
    public static final int TRANSACTION_CONTAINER_TYPE = 3;


    @Override
    public IdentifiedDataSerializable create(int typeId) {
//        if (typeId == TRANSACTION_TYPE)
//            return new Transaction();
//        else if (typeId == TRANSACTION_NODE_TYPE)
//            return new TransactionHistoryNode();
//        else if (typeId == TRANSACTION_CONTAINER_TYPE)
//            return new TransactionHistoryContainer();
//        else
//            return null;
        return null;
    }
}