package com.hazelcast.certification.util;

import com.hazelcast.aggregation.Aggregator;
import com.hazelcast.certification.domain.FraudCheck;
import com.hazelcast.certification.domain.Stats;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class StatsAggregator extends  Aggregator<Map.Entry<String, LinkedList<Transaction>>, Stats> implements DataSerializable {

    private long sinceTimestamp;

    private Stats result;

    public StatsAggregator(){
        // need this for serialiation
        sinceTimestamp = 0;
        result = new Stats();
    }

    public StatsAggregator(long since){
        this.sinceTimestamp = since;
        this.result = new Stats();
    }

    @Override
    public void accumulate(Map.Entry<String, LinkedList<Transaction>> entry) {
        LinkedList<Transaction> history = entry.getValue();
        Iterator<Transaction> iterator = history.descendingIterator();
        while(iterator.hasNext()){
            FraudCheck fraudCheckResult = iterator.next().getFraudCheck();
            if (fraudCheckResult == null || fraudCheckResult.getTimestamp() < sinceTimestamp) {
                break;   /// BREAK
            }

            result.accumulate(fraudCheckResult.isFraudulent());
        }
    }

    @Override
    public void combine(Aggregator aggregator) {
        StatsAggregator statsAggregator = StatsAggregator.class.cast(aggregator);
        result.combine(statsAggregator.result);
    }

    @Override
    public Stats aggregate() {
        return result;
    }


    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeLong(sinceTimestamp);
        objectDataOutput.writeObject(result);
    }

    public void readData(ObjectDataInput objectDataInput) throws IOException {
        sinceTimestamp = objectDataInput.readLong();
        result = objectDataInput.readObject();
    }
}
