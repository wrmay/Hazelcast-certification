import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.certification.domain.TransactionDataSerializableFactory;
import com.hazelcast.certification.server.ProcessTransactionEntryProcessor;
import com.hazelcast.certification.domain.TransactionHistoryContainer;
import com.hazelcast.certification.util.TransactionsUtil;
import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

public class TransactionHistoryContainerTest {

    @Test
    public void emptyTest(){
        TransactionHistoryContainer container = new TransactionHistoryContainer(90);
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
        TransactionHistoryContainer container = new TransactionHistoryContainer(90);
        Transaction t1 = generateTransaction(1);
        container.add(t1);


        Iterator <Transaction>it = container.iterator();
        Assertions.assertTrue(it.hasNext());

        Transaction t = it.next();
        Assertions.assertEquals(t1, t);

        Assertions.assertFalse(it.hasNext());
    }


    @Test
    public void multiEntryTest(){
        TransactionHistoryContainer container = new TransactionHistoryContainer(90);

        Transaction t1 = generateTransaction(1);
        container.add(t1);

        Transaction t2 = generateTransaction(2);
        container.add(t2);


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
        TransactionHistoryContainer container = new TransactionHistoryContainer(90);

        Transaction oldTransaction = generateTransaction(1);
        oldTransaction.setTimeStamp(DateTime.now().minusDays(91).getMillis());
        container.add(oldTransaction);

        Transaction t1 = generateTransaction(2);
        container.add(t1);

        Transaction t2 = generateTransaction(3);
        container.add(t2);


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
    public void purgeTest(){
        TransactionHistoryContainer container = new TransactionHistoryContainer(90);

        Transaction oldTransaction = generateTransaction(1);
        oldTransaction.setTimeStamp(DateTime.now().minusDays(91).getMillis());
        container.add(oldTransaction);

        Transaction t1 = generateTransaction(2);
        container.add(t1);

        Transaction t2 = generateTransaction(3);
        container.add(t2);


        Assertions.assertEquals(3,container.size());

        Iterator <Transaction>it = container.iterator();
        Assertions.assertTrue(it.hasNext());

        Transaction t = it.next();
        Assertions.assertEquals(t2, t);

        Assertions.assertTrue(it.hasNext());

        t = it.next();
        Assertions.assertEquals(t1, t);

        Assertions.assertFalse(it.hasNext());

        Assertions.assertEquals(3, container.size());
        container.purge();
        Assertions.assertEquals(2,container.size());
    }

    @Test
    public void serializationTest(){
        Config hazelcastConfig = new Config();
        hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        hazelcastConfig.getSerializationConfig().addDataSerializableFactory(TransactionDataSerializableFactory.FACTORY_ID, new TransactionDataSerializableFactory());
        MapConfig testMapConfig = new MapConfig("test");
        testMapConfig.setInMemoryFormat(InMemoryFormat.BINARY);
        hazelcastConfig.addMapConfig(testMapConfig);
        hazelcastConfig.setLicenseKey("ENTERPRISE_HD#30Nodes#ni21yjqkNJgulZPMSDHXTKQm9O0f5Y8bABECdUGWw661100000107100010211111000221001900900090130");
        hazelcastConfig.getSecurityConfig().setEnabled(false);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(hazelcastConfig);
        IMap<String, TransactionHistoryContainer> testMap = hz.getMap("test");


        TransactionHistoryContainer container = new TransactionHistoryContainer(90);

        Transaction oldTransaction = generateTransaction(1);
        oldTransaction.setTimeStamp(DateTime.now().minusDays(91).getMillis());
        container.add(oldTransaction);

        Transaction t1 = generateTransaction(2);
        container.add(t1);

        Transaction t2 = generateTransaction(3);
        container.add(t2);

        testMap.set("abc",container);
        TransactionHistoryContainer retrievedContainer = testMap.get("abc");


        Iterator <Transaction>it = retrievedContainer.iterator();
        Assertions.assertTrue(it.hasNext());

        Transaction t = it.next();
        Assertions.assertEquals(t2, t);

        Assertions.assertTrue(it.hasNext());

        t = it.next();
        Assertions.assertEquals(t1, t);

        Assertions.assertFalse(it.hasNext());
        hz.shutdown();
    }
}
