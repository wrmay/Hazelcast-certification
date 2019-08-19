import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.certification.server.ProcessTransactionEntryProcessor;
import com.hazelcast.certification.util.TransactionHistoryContainer;
import com.hazelcast.certification.util.TransactionsUtil;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.LinkedList;

public class TransactionHistoryContainerTest {

    @Test
    public void emptyTest(){
        LinkedList<Transaction> emptyList = new LinkedList<Transaction>();
        TransactionHistoryContainer container = new TransactionHistoryContainer(emptyList, 90);
        Assertions.assertFalse(container.iterator().hasNext());
    }

    private Transaction generateTransaction(int seq){
        TransactionsUtil txnGenerator = new TransactionsUtil();

        String ccnum = txnGenerator.generateCreditCardNumber(seq);
        String txnString = txnGenerator.createAndGetCreditCardTransaction(ccnum, seq, true);

        ProcessTransactionEntryProcessor ep = new ProcessTransactionEntryProcessor(txnString);
        return(ep.prepareTransaction(txnString));
    }

    @Test
    public void singleEntryTest(){
        LinkedList<Transaction> list = new LinkedList<>();

        Transaction t1 = generateTransaction(1);
        list.addLast(t1);

        TransactionHistoryContainer container = new TransactionHistoryContainer(list,90);

        Iterator <Transaction>it = container.iterator();
        Assertions.assertTrue(it.hasNext());

        Transaction t = it.next();
        Assertions.assertEquals(t1, t);

        Assertions.assertFalse(it.hasNext());
    }


    @Test
    public void multiEntryTest(){
        LinkedList<Transaction> list = new LinkedList<>();

        Transaction t1 = generateTransaction(1);
        list.addLast(t1);

        Transaction t2 = generateTransaction(2);
        list.addLast(t2);

        TransactionHistoryContainer container = new TransactionHistoryContainer(list,90);

        Iterator <Transaction>it = container.iterator();
        Assertions.assertTrue(it.hasNext());

        Transaction t = it.next();
        Assertions.assertEquals(t2, t);

        Assertions.assertTrue(it.hasNext());

       t = it.next();
        Assertions.assertEquals(t1, t);

        Assertions.assertFalse(it.hasNext());
    }

    @Test
    public void oldTransactionTest(){
        LinkedList<Transaction> list = new LinkedList<>();

        Transaction oldTransaction = generateTransaction(1);
        oldTransaction.setTimeStamp(DateTime.now().minusDays(91).getMillis());
        list.addLast(oldTransaction);

        Transaction t1 = generateTransaction(2);
        list.addLast(t1);

        Transaction t2 = generateTransaction(3);
        list.addLast(t2);

        TransactionHistoryContainer container = new TransactionHistoryContainer(list,90);

        Iterator <Transaction>it = container.iterator();
        Assertions.assertTrue(it.hasNext());

        Transaction t = it.next();
        Assertions.assertEquals(t2, t);

        Assertions.assertTrue(it.hasNext());

        t = it.next();
        Assertions.assertEquals(t1, t);

        Assertions.assertFalse(it.hasNext());
    }
}
