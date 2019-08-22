package com.hazelcast.certification.util;

import com.hazelcast.certification.domain.Transaction;
import org.joda.time.DateTime;

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

public class TransactionHistoryContainer implements Iterable<Transaction> {

    TransactionHistoryNode lastTransaction;

    long dateCutoff;
    int size;

    public TransactionHistoryContainer(int days){
        dateCutoff = DateTime.now().minusDays(days).getMillis();
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


    // this assumes that t is actually an instance (not a separate but identical instance). Since this is actually the case
    // and since some of the rules call this, it seems justified.
    public boolean remove(Transaction t){
        if (lastTransaction == null) return false;

        if (t == lastTransaction.transaction) {
            lastTransaction = lastTransaction.previous;
            this.size -= 1;
            return true;
        }

        TransactionHistoryNode currNode = lastTransaction;
        while(currNode.previous != null){
            if (t == currNode.previous.transaction){
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

    public class TransactionHistoryIterator implements Iterator<Transaction> {

        private TransactionHistoryNode currentNode;

        public TransactionHistoryIterator(){
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

    private static class TransactionHistoryNode {
        public TransactionHistoryNode(){

        }

        public TransactionHistoryNode(Transaction t){
            this.transaction = t;
        }

        public Transaction transaction;
        public TransactionHistoryNode previous;  // previous in the sense of earlier
    }

}
