package com.hazelcast.certification.util;

import com.hazelcast.certification.domain.Transaction;
import org.joda.time.DateTime;

import java.util.*;

/**
 * The container  provides an iterator that limits the scope to only Transactions newer than X days.
 *
 * It assumes the underlying collection is in ascending date order.  The iterator provided by this container
 * iterates in reverse order and stops iterating when it reaches a transaction older than the specified number
 * of days.
 */

public class TransactionHistoryContainer implements Iterable<Transaction> {

    LinkedList<Transaction> transactionList;

    long dateCutoff;

    public TransactionHistoryContainer(LinkedList<Transaction> transactionList, int days){
        this.transactionList = transactionList;
        dateCutoff = DateTime.now().minusDays(days).getMillis();
    }


    // keep the list ordered
    public void add(Transaction t){
        ListIterator<Transaction> it = transactionList.listIterator(transactionList.size());
        while(it.hasPrevious()){
            Transaction p = it.previous();
            if (t.compareTo(p) > 0) {
                it.add(t);
                return;  // RETURN
            }
        }

        transactionList.addFirst(t);
    }


    public void remove(Transaction t){
         transactionList.remove(t);
    }

    public int size(){
        return transactionList.size();
    }

    public Transaction get(int i){
        return transactionList.get(i);
    }

    @Override
    public Iterator<Transaction> iterator() {
        return new TransactionHistoryIterator();
    }

    public class TransactionHistoryIterator implements Iterator<Transaction> {

        private Iterator<Transaction> it;
        private Transaction next;

        private void advance(){
            if (it.hasNext())
                next = it.next();
            else
                next = null;

            if (next != null){
                if (next.getTimeStamp() < dateCutoff) next = null;
            }
        }

        public TransactionHistoryIterator(){
            it = transactionList.descendingIterator();
            advance();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Transaction next() {
            if (next == null) throw new NoSuchElementException();
            Transaction result = next;
            advance();
            return result;
        }
    }


}
