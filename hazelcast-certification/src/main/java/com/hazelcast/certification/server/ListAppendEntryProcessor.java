package com.hazelcast.certification.server;

import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;

public class ListAppendEntryProcessor<K,T extends Serializable> implements EntryProcessor<K, LinkedList<T>>, EntryBackupProcessor<K, LinkedList<T>> {

    T listItem;

    public ListAppendEntryProcessor(){

    }

    public ListAppendEntryProcessor(T item){
        this.listItem = item;
    }


    public void setItem(T i){
        this.listItem = i;
    }

    @Override
    public void processBackup(Map.Entry<K, LinkedList<T>> entry) {
        process(entry);
    }

    @Override
    public Object process(Map.Entry<K, LinkedList<T>> entry) {
        LinkedList<T> value = entry.getValue();
        if (value == null) value = new LinkedList<>();
        value.addLast(listItem);
        entry.setValue(value);
        return value.size();
    }

    @Override
    public EntryBackupProcessor<K, LinkedList<T>> getBackupProcessor() {
        return this;
    }
}
