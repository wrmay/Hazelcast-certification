package hazelcast.cert.com.util;

import hazelcast.cert.com.domain.Transaction;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TransactionsUtil {

	private static Random countryCodeRandom = new Random(1);
	private static Random cityCodeRandom = new Random(1);
	private static Random merchantTypeRandom = new Random(1);
	private static Random txnAmountRandom = new Random(100);

	/**
	 * Create dummy transaction for the given credit card number
	 * 
	 * @param creditCardNumber
	 *            Credit Card number for which the transactions to be created
	 * @return
	 */
	public static Transaction createCreditCardTransaction(String creditCardNumber, int code) {

		Transaction txn = new Transaction();
		String countryCode = generateCountryCode();
		txn.setCreditCardNumber(creditCardNumber);
		txn.setTimeStamp(generateTimeStamp());
		txn.setCountryCode(countryCode);
		txn.setResponseCode(generateResponseCode(code));
		txn.setTxnAmt(generateTxnAmount());
		// Currency is same as CountryCode
		txn.setTxnCurrency(countryCode);
		txn.setMerchantType(generateMerchantType());
		txn.setTxnCity(generateCityCode());
		txn.setTxnCode(generateTxnCode(code));

		return txn;
	}

	/**
	 * Create dummy transactions for the given credit card number
	 * 
	 * @param creditCardNumber
	 *            Credit Card number for which the transactions to be created
	 * @param txnCount
	 *            number of historical transactions to create
	 * @return
	 */
	public static List<Transaction> createCreditCardTransactions(
			String creditCardNumber, int txnCount) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		for (int j = 0; j < txnCount; j++) {

			Transaction txn = new Transaction();
			String countryCode = generateCountryCode();
			txn.setCreditCardNumber(creditCardNumber);
			txn.setTimeStamp(generateTimeStamp());
			txn.setCountryCode(countryCode);
			txn.setResponseCode(generateResponseCode(j));
			txn.setTxnAmt(generateTxnAmount());
			// Currency is same as CountryCode
			txn.setTxnCurrency(countryCode);
			txn.setMerchantType(generateMerchantType());
			txn.setTxnCity(generateCityCode());
			txn.setTxnCode(generateTxnCode(j));

			transactions.add(txn);
		}
		return transactions;
	}

	// card number : 14-bits, 00000001, 000~30000000, totally 30 Million cards
	public static String generateCreditCardNumber(int id) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("000000");
		if (id < 10)
			buffer.append("0000000" + id);
		else if (id > 10 && id < 100)
			buffer.append("000000" + id);
		else if (id > 100 && id < 1000)
			buffer.append("00000" + id);
		else if (id > 1000 && id < 10000)
			buffer.append("0000" + id);
		else if (id > 10000 && id < 100000)
			buffer.append("000" + id);
		else if (id > 100000 && id < 1000000)
			buffer.append("00" + id);
		else if (id > 1000000 && id < 10000000)
			buffer.append("0" + id);
		else
			buffer.append(id);
		return buffer.toString();
	}

	// last 90 days
	private static String generateTimeStamp() {
		long offset = Timestamp.valueOf("2013-01-01 00:00:00").getTime();
		long end = Timestamp.valueOf("2013-04-01 00:00:00").getTime();
		long diff = end - offset + 1;
		Timestamp rand = new Timestamp(offset + (long) (Math.random() * diff));
		return rand.toString();
	}

	private static String generateTxnCode(int temp) {
		if (temp < 10)
			return "0000" + temp;
		if (temp > 10 && temp < 100)
			return "000" + temp;
		if (temp == 100)
			return "00" + temp;
		return String.valueOf(temp);
	}

	// 001-200
	private static String generateCountryCode() {
		int number = countryCodeRandom.nextInt(200);
		if (number < 10)
			return "00" + number;
		if (number > 10 && number < 100)
			return "0" + number;
		return String.valueOf(number);
	}

	// 95% 00 else random 2-bits
	private static String generateResponseCode(int count) {
		Random random = new Random(10);
		if (count % 95 == 0)
			return String.valueOf(random.nextInt(20));

		return "00";
	}

	// 100-50000 random
	private static String generateTxnAmount() {
		return String.valueOf(txnAmountRandom.nextInt(50000));
	}

	// 0001-0500
	private static String generateMerchantType() {
		int merchT = merchantTypeRandom.nextInt(500);
		if (merchT < 10)
			return "000" + merchT;
		if (merchT > 10 && merchT < 100)
			return "00" + merchT;
		return String.valueOf("0" + merchT);
	}

	// 00001-10000
	private static String generateCityCode() {
		int temp = cityCodeRandom.nextInt(10000);
		if (temp < 10)
			return "0000" + temp;
		if (temp > 10 && temp < 100)
			return "000" + temp;
		if (temp > 100 && temp < 1000)
			return "00" + temp;
		if (temp > 1000 && temp < 10000)
			return "0" + temp;
		return String.valueOf(temp);
	}
}
