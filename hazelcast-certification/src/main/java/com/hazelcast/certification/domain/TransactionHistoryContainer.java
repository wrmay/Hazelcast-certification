package com.hazelcast.certification.domain;

import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import org.joda.time.DateTime;

import java.io.IOException;
import java.nio.channels.NoConnectionPendingException;
import java.util.*;

/**
 * The container  provides an iterator that limits the scope to only Transactions newer than X days.
 *
 * It is implemented as a singly linked list (as opposed to doubly) to save space when storing the
 * transaction history.  It is  optimized to work with the rule engine.
 *
 * It is not safe for multiple threads to access the same instance concurrently (doesn't need to  be)
 */

public class TransactionHistoryContainer implements Iterable<Transaction>, IdentifiedDataSerializable {

    TransactionHistoryNode lastTransaction;

    int daysToKeep;
    int size;

    public TransactionHistoryContainer(){
    }

    public TransactionHistoryContainer(int days){
        this.daysToKeep = days;
        lastTransaction = null;
        size = 0;
    }


    // keep the list in descending order by transaction date
    public void add(Transaction newTransaction){
        if (lastTransaction == null){
            lastTransaction = new TransactionHistoryNode(newTransaction);
        } else {
            if (newTransaction.compareTo(lastTransaction.transaction) >= 0){
                TransactionHistoryNode newNode = new TransactionHistoryNode(newTransaction);
                newNode.previous = lastTransaction;
                lastTransaction = newNode;
            } else {
                TransactionHistoryNode currNode = lastTransaction;
                while(currNode.previous != null){
                    if ( newTransaction.compareTo(currNode.previous.transaction) > 0){
                        TransactionHistoryNode newNode = new TransactionHistoryNode(newTransaction);
                        newNode.previous = currNode.previous;
                        currNode.previous = newNode;
                        break;
                    }
                    currNode = currNode.previous;
                }
                if (currNode.previous == null){  // it is last in the singly linked list / earliest in time
                    currNode.previous = new TransactionHistoryNode(newTransaction);
                }
            }
        }
        this.size += 1;
    }

    public int purge(){
        long dateCutoff = DateTime.now().minusDays(daysToKeep).getMillis();

        if (lastTransaction == null) return 0;


        if (lastTransaction.transaction.getTimeStamp() <= dateCutoff){
            lastTransaction = null;
            int removed = size;
            size = 0;
            return removed; //RETURN
        }

        TransactionHistoryNode currNode = lastTransaction;
        while(currNode.previous != null){
            if (currNode.previous.transaction.getTimeStamp() <= dateCutoff){
                // remove currNode.previous and everything after it.
                // count how many things will be removed;

                int count = 1;
                TransactionHistoryNode deadNode = currNode.previous;
                while(deadNode.previous != null){
                    ++count;
                    deadNode = deadNode.previous;
                }
                currNode.previous = null;
                size -= count;
                return count; //RETURN
            }

            currNode = currNode.previous;
        }

        return 0;
    }

    public boolean remove(Transaction t){
        if (lastTransaction == null) return false;

        if (t.equals(lastTransaction.transaction)) {
            lastTransaction = lastTransaction.previous;
            this.size -= 1;
            return true;
        }

        TransactionHistoryNode currNode = lastTransaction;
        while(currNode.previous != null){
            if (t.equals(currNode.previous.transaction)){
                currNode.previous = currNode.previous.previous;
                this.size -= 1;
                return true;
            }
            currNode = currNode.previous;
        }

        return false;  // not found
    }

    // includes all contents, not just items < X days old
    public int size(){
        return size;
    }

    // 0 is latest txn, 1 is next, etc.
    // includes all contents, not just items < X days old
    public Transaction get(int i){
        if (i< 0 || i > this.size ) throw new IndexOutOfBoundsException();

        Iterator<Transaction> it = this.iterator();
        Transaction result = null;
        int j=-1;
        while(it.hasNext()){
            result = it.next();
            ++j;
            if (j == i) break;
        }

        return result;
    }

    @Override
    public TransactionHistoryIterator iterator() {
        return new TransactionHistoryIterator();
    }

    @Override
    public int getFactoryId() {
        return TransactionDataSerializableFactory.FACTORY_ID;
    }

    @Override
    public int getId() {
        return TransactionDataSerializableFactory.TRANSACTION_CONTAINER_TYPE;
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeObject(lastTransaction);
        objectDataOutput.writeInt(daysToKeep);
        objectDataOutput.writeInt(size);

    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        lastTransaction = objectDataInput.readObject();
        daysToKeep = objectDataInput.readInt();
        size = objectDataInput.readInt();
    }

    public class TransactionHistoryIterator implements Iterator<Transaction> {

        private TransactionHistoryNode currentNode;
        private long dateCutoff;

        public TransactionHistoryIterator()
        {
            dateCutoff = DateTime.now().minusDays(daysToKeep).getMillis();
            currentNode = null;  // this means that "next" is the last in the list
        }


        @Override
        public boolean hasNext() {  // remember - next means earlier in this case
            if (currentNode == null) return lastTransaction != null && lastTransaction.transaction.getTimeStamp() > dateCutoff;

            return currentNode.previous != null  && currentNode.previous.transaction.getTimeStamp() > dateCutoff;
        }

        @Override
        public Transaction next() {
            if (lastTransaction == null) throw new NoSuchElementException();
            if (currentNode == null){
                currentNode = lastTransaction;
            } else {
                currentNode = currentNode.previous;
            }

            if (currentNode == null) throw new NoSuchElementException();

            if (currentNode.transaction.getTimeStamp() <= dateCutoff) throw new NoSuchElementException();

            return currentNode.transaction;
        }
    }


}
