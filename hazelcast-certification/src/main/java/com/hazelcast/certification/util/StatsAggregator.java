package com.hazelcast.certification.util;

import com.hazelcast.aggregation.Aggregator;
import com.hazelcast.certification.domain.Stats;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Map;

public class StatsAggregator extends  Aggregator<Map.Entry<String,Stats>, Stats> implements DataSerializable {

    private Stats stats;

    public StatsAggregator(){
        stats = new Stats();
    }

    public void accumulate(Map.Entry<String, Stats> stringStatsEntry) {
        stats.accumulate(stringStatsEntry.getValue());
    }

    public void combine(Aggregator aggregator) {
        this.stats.accumulate(StatsAggregator.class.cast(aggregator).stats);
    }

    public Stats aggregate() {
        return stats;
    }

    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeObject(stats);
    }

    public void readData(ObjectDataInput objectDataInput) throws IOException {
        stats = objectDataInput.readObject();
    }
}
