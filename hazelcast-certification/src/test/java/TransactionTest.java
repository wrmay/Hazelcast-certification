import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.certification.server.ProcessTransactionEntryProcessor;
import com.hazelcast.certification.util.TransactionsUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransactionTest {

    @Test
    public void test1(){
        Transaction t = new Transaction();

        // first just check every string field comes back the same
        String cc = "01234567890123";
        String country = "USA";
        String responseCode = "99";
        String merchantType= "8888";
        String city = "77777";
        String txnCode = "66666";
        String currency = "555";

        t.setCreditCardNumber(cc);
        t.setCountryCode(country);
        t.setResponseCode(responseCode);
        t.setMerchantType(merchantType);
        t.setTxnCity(city);
        t.setTxnCode(txnCode);
        t.setTxnCurrency(currency);

        Assertions.assertEquals(cc, t.getCreditCardNumber());
        Assertions.assertEquals(country, t.getCountryCode());
        Assertions.assertEquals(responseCode, t.getResponseCode());
        Assertions.assertEquals(merchantType, t.getMerchantType());
        Assertions.assertEquals(city, t.getTxnCity());
        Assertions.assertEquals(txnCode, t.getTxnCode());
        Assertions.assertEquals(currency, t.getTxnCurrency());

    }

    /*
     * This is a facsimile of the code that is executed to generate and parse
     * a Transaction
     */
    @Test
    public void test2(){

        // from TransactionsUtil.createAndGetCreditCardTransaction
        TransactionsUtil txnUtil = new TransactionsUtil();

        String ccNumber = "00000000001234";
        String countryCode= txnUtil.generateCountryCode();
        long ts = txnUtil.generateTimeStamp();
        String responseCode = txnUtil.generateResponseCode(77);
        int amount = txnUtil.generateTxnAmount();
        String type = txnUtil.generateMerchantType();
        String city = txnUtil.generateCityCode();
        String txCode = txnUtil.generateTxnCode(44);

        StringBuffer txn = new StringBuffer();
        txn.append(ccNumber)
                .append(",").append(ts)
                .append(",").append(countryCode)
                .append(",").append(responseCode)
                .append(",").append(amount)
                .append(",").append(countryCode)  // yes, currency code == country code
                .append(",").append(type)
                .append(",").append(city)
                .append(",").append(txCode)
                .append(new String(new byte[99 - txn.toString().getBytes().length]))
                .append("\n");

        String rawTxnString = txn.toString();

        // from FraudDetectionClient.process
        int z = rawTxnString.indexOf(0);
        Assertions.assertTrue(z > 0);
        String txnString = rawTxnString.substring(0, z);

        // from ProcessTransactionEntryProcessor
        ProcessTransactionEntryProcessor ep = new ProcessTransactionEntryProcessor(txnString);
        Transaction t = ep.prepareTransaction(txnString);

        Assertions.assertEquals(ccNumber, t.getCreditCardNumber());
        Assertions.assertEquals(ts, t.getTimeStamp());
        Assertions.assertEquals(countryCode, t.getCountryCode());
        Assertions.assertEquals(responseCode, t.getResponseCode());
        Assertions.assertEquals(amount, t.getTxnAmt());
        Assertions.assertEquals(type, t.getMerchantType());
        Assertions.assertEquals(city, t.getTxnCity());
        Assertions.assertEquals(txCode, t.getTxnCode());
        Assertions.assertEquals(countryCode, t.getTxnCurrency());  // yes, this is right

    }

}
