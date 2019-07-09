package com.hazelcast.certification.business.ruleengine;

import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.util.List;

public final class RuleEngine {

	private final static ILogger log = Logger.getLogger(RuleEngine.class);

	private Transaction currentTxn;
	private List<Transaction> historicalTxns;
	private boolean detectionResult;
	private int failedTest;
	private DateTime dateTime;

	public RuleEngine(Transaction currentTxn, List<Transaction> historicalTxns) {
		this.currentTxn = currentTxn;
		this.historicalTxns = historicalTxns;
		detectionResult = false;


		dateTime = new DateTime(currentTxn.getTimeStamp());
	}

	// should short-circuit the rule execution here
	public void executeRules() {
		try {
			rule01();
			rule02();
			rule03();
			rule04();
			rule05();
			rule06();
			rule07();
			rule08();
			rule09();
			rule10();
			rule11();
			rule12();
			rule13();
			rule14();
			rule15();
			rule16();
			rule17();
			rule18();
			rule19();
			rule20();
			rule21();
			rule22();
			rule23();
			rule24();
			rule25();
			rule26();
			rule27();
			rule28();
			rule29();
			rule30();
			rule31();
			rule32();
			rule33();
			rule34();
			rule35();
			rule36();
			rule37();
			rule38();
			rule39();
			rule40();
			rule41();
			rule42();
			rule43();
			rule44();
			rule45();
			rule46();
			rule47();
			rule48();
			rule49();
		} catch (FraudException e) {
			this.detectionResult = true;
			this.failedTest = e.getFailedTest();
			//log.warning("FRAUD: " + e.getFailedTest());
		} catch (ParseException e) {
			log.severe(e);
		}
	}

	private void rule01() {
		if ("111".equals(currentTxn.getCountryCode())
				&& currentTxn.getTxnAmt() > 200000
				&& "0022".equals(currentTxn.getMerchantType()))
			throw new FraudException(1);
	}

	private void rule02() {
		if ("121".equals(currentTxn.getCountryCode())
				&& (!"00".equals(currentTxn.getResponseCode())))
			throw new FraudException(2);
	}

	private void rule03() {
		if ("0011".equals(currentTxn.getMerchantType())
				&& ((currentTxn.getTxnAmt() > 1000 && "011"
						.equals(currentTxn.getTxnCurrency())) || currentTxn.getTxnAmt() > 1000 && "011"
						.equals(currentTxn.getTxnCurrency())))
			throw new FraudException(3);
	}

	private void rule04() {
		if (currentTxn.getTxnAmt() > 100000
				&& "1000".equals(currentTxn.getTxnCode()))
			throw new FraudException(4);
	}

	private void rule05() {
		if (currentTxn.getTxnAmt() > 100000
				&& "00004000222100".equals(currentTxn.getCreditCardNumber()))
			throw new FraudException(5);
	}

	private void rule06() {
		if (("000112340000444422".equals(currentTxn.getCreditCardNumber()) || "000112340000444422"
				.equals(currentTxn.getCreditCardNumber()))
				&& "2000".equals(currentTxn.getMerchantType()))
			throw new FraudException(6);
	}

	private void rule07() {
		if (("0001".equals(currentTxn.getMerchantType()) || "2222"
				.equals(currentTxn.getMerchantType()))
				&& "221".equals(currentTxn.getCountryCode()))
			throw new FraudException(7);
	}

	private void rule08() {
		if ("3333".equals(currentTxn.getTxnCode())
				&& currentTxn.getTxnAmt() > 10000
				&& "2222".equals(currentTxn.getMerchantType()))
			throw new FraudException(8);
	}

	private void rule09() throws ParseException {
		if (dateTime.getHourOfDay() > 0 && dateTime.getHourOfDay() < 4
				&& currentTxn.getTxnAmt() >= 10000)
			throw new FraudException(9);
	}

	private void rule10() throws ParseException {
		if (dateTime.getHourOfDay() > 0 && dateTime.getHourOfDay() < 4
				&& "0101".equals(currentTxn.getMerchantType())
				&& currentTxn.getTxnAmt() >= 10000)
			throw new FraudException(10);
	}

	private void rule11() {
		if (("221".equals(currentTxn.getCountryCode()) || "444"
				.equals(currentTxn.getCountryCode()))
				&& ("0001".equals(currentTxn.getMerchantType()) || "2222"
						.equals(currentTxn.getMerchantType())))
			throw new FraudException(11);
	}

	private void rule12() {
		if ("00004000222333".equals(currentTxn.getCreditCardNumber())
				&& currentTxn.getTxnAmt() > 50000)
			throw new FraudException(12);
	}

	private void rule13() {
		if (("000112340000444444".equals(currentTxn.getCreditCardNumber()) || "003300030044442211"
				.equals(currentTxn.getCountryCode()))
				&& "2200".equals(currentTxn.getMerchantType()))
			throw new FraudException(13);
	}

	private void rule14() {
		if (("000112340000444444".equals(currentTxn.getCreditCardNumber()) || "003300030044442211"
				.equals(currentTxn.getCountryCode()))
				&& !"2222".equals(currentTxn.getTxnCode()))
			throw new FraudException(14);
	}

	private void rule15() throws ParseException {
		if (dateTime.getHourOfDay() > 0 && dateTime.getHourOfDay() < 4
				&& "2222".equals(currentTxn.getMerchantType())
				&& currentTxn.getTxnAmt() >= 10000)
			throw new FraudException(15);
	}

	private void rule16() throws ParseException {
		if (dateTime.getHourOfDay() > 0 && dateTime.getHourOfDay() < 4
				&& "2222".equals(currentTxn.getTxnCode())
				&& currentTxn.getTxnAmt() >= 10000)
			throw new FraudException(16);
	}

	private void rule17() throws ParseException {
		if (dateTime.getMonthOfYear() == 3 && dateTime.getDayOfMonth() == 25
				&& "2222".equals(currentTxn.getTxnCode())
				&& currentTxn.getTxnAmt() >= 10000)
			throw new FraudException(17);

	}

	private void rule18() throws ParseException {
		if (dateTime.getMonthOfYear() == 3 && dateTime.getDayOfMonth() == 25
				&& "2222".equals(currentTxn.getMerchantType())
				&& currentTxn.getTxnAmt() >= 10000)
			throw new FraudException(18);

	}

	private void rule19() throws ParseException {
		if ("3344".equals(currentTxn.getTxnCode())
				&& currentTxn.getTxnAmt() >= 1000000)
			throw new FraudException(19);

	}

	private void rule20() throws ParseException {
		if ("1001".equals(currentTxn.getTxnCode())
				&& currentTxn.getTxnAmt() >= 500000)
			throw new FraudException(20);

	}

	private void rule21() throws ParseException {
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		dateTimeLocal = dateTimeLocal.minusHours(1);
		int n_txn_amt = 0;
		for (Transaction txn : historicalTxns) {
			DateTime historicalDateTime = new DateTime(txn.getTimeStamp());
			if (historicalDateTime.isAfter(dateTimeLocal))
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
		}
		if ("00004000222333".equals(currentTxn.getCreditCardNumber())
				&& n_txn_amt > 500000)
			throw new FraudException(21);

	}

	private void rule22()
			throws ParseException {
		int n_txn_amt = 0;

		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		dateTimeLocal = dateTimeLocal.minusHours(5);
		for (Transaction txn : historicalTxns) {
			DateTime historicalDT = new DateTime(txn.getTimeStamp());
			if (historicalDT.isAfter(dateTimeLocal))
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
		}
		if ("3333".equals(currentTxn.getTxnCode()) && n_txn_amt > 1000000
				&& "2222".equals(currentTxn.getMerchantType()))
			throw new FraudException(22);

	}

	private void rule23()
			throws ParseException {
		int count = 0;

		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		dateTimeLocal = dateTimeLocal.minusHours(2);
		for (Transaction txn : historicalTxns) {
			DateTime historicalDT = new DateTime(txn.getTimeStamp());
			if (historicalDT.isAfter(dateTimeLocal))
				count++;
		}
		if (count > 100)
			throw new FraudException(23);

	}

	private void rule24()
			throws ParseException {
		int count = 0;
		int n_txn_amt = 0;

		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		dateTimeLocal = dateTimeLocal.minusHours(3);
		for (Transaction txn : historicalTxns) {
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal) && "3344".equals(currentTxn.getTxnCode())) {
				count++;
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
		}
		if (count > 10 && n_txn_amt >= 10000000)
			throw new FraudException(24);

	}

	private void rule25()
			throws ParseException {
		int n_txn_amt = 0;
		int n_txn_amt1 = 0;

		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusHours(8);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)
					&& "0011".equals(currentTxn.getMerchantType())
					&& ("011".equals(currentTxn.getTxnCurrency()))) {
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
			if (txn_time.isAfter(dateTimeLocal)
					&& "0011".equals(currentTxn.getMerchantType())
					&& ("022".equals(currentTxn.getTxnCurrency()))) {
				n_txn_amt1 = n_txn_amt1 + txn.getTxnAmt();
			}
		}
		if (n_txn_amt > 10000 || n_txn_amt1 > 20000)
			throw new FraudException(25);

	}

	private void rule26()
			throws ParseException {
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(1);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)) {
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
		}
		if ("00004000222333".equals(currentTxn.getCreditCardNumber())
				&& n_txn_amt >= 500000)
			throw new FraudException(26);

	}

	private void rule27()
			throws ParseException {
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(1);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal) && "3333".equals(txn.getTxnCode())
					&& "2222".equals(txn.getMerchantType())) {
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
		}
		if (n_txn_amt >= 1000000)
			throw new FraudException(27);

	}

	private void rule28()
			throws ParseException {
		int count = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(1);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)) {
				count++;
			}
		}
		if (count >= 300)
			throw new FraudException(28);

	}

	private void rule29()
			throws ParseException {
		int count = 0;
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(1);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal) && "3344".equals(currentTxn.getTxnCode())) {
				count++;
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
		}
		if (count > 10 && n_txn_amt >= 10000000)
			throw new FraudException(29);

	}

	private void rule30()
			throws ParseException {
		int n_txn_amt = 0;
		int n_txn_amt1 = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());

		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(1);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)
					&& "0011".equals(currentTxn.getMerchantType())
					&& ("011".equals(currentTxn.getTxnCurrency()))) {
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
			if (txn_time.isAfter(dateTimeLocal)
					&& "0011".equals(currentTxn.getMerchantType())
					&& ("022".equals(currentTxn.getTxnCurrency()))) {
				n_txn_amt1 = n_txn_amt1 + txn.getTxnAmt();
			}
		}
		if (n_txn_amt > 10000 || n_txn_amt1 > 20000)
			throw new FraudException(30);

	}

	private void rule31()
			throws ParseException {
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(10);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)) {
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
		}
		if ("00004000222333".equals(currentTxn.getCreditCardNumber())
				&& n_txn_amt > 500000)
			throw new FraudException(31);

	}

	private void rule32()
			throws ParseException {
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(11);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal) && "3333".equals(txn.getTxnCode())
					&& "2222".equals(txn.getMerchantType())) {
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
		}
		if (n_txn_amt > 500000)
			throw new FraudException(32);

	}

	private void rule33()
			throws ParseException {
		int count = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(12);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)) {
				count++;
			}
		}
		if (count >= 400)
			throw new FraudException(33);

	}

	private void rule34()
			throws ParseException {
		int count = 0;
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(13);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal) && "3344".equals(currentTxn.getTxnCode())) {
				count++;
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
		}
		if (count > 10 && n_txn_amt >= 10000000)
			throw new FraudException(34);

	}

	private void rule35()
			throws ParseException {
		int n_txn_amt = 0;
		int n_txn_amt1 = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(14);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)
					&& "0011".equals(currentTxn.getMerchantType())
					&& ("011".equals(currentTxn.getTxnCurrency()))) {
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
			if (txn_time.isAfter(dateTimeLocal)
					&& "0011".equals(currentTxn.getMerchantType())
					&& ("022".equals(currentTxn.getTxnCurrency()))) {
				n_txn_amt1 = n_txn_amt1 + txn.getTxnAmt();
			}
		}
		if (n_txn_amt > 10000 || n_txn_amt1 > 20000)
			throw new FraudException(35);

	}

	private void rule36()
			throws ParseException {
		int count = 0;
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime()).minusDays(60);
		for (Transaction txn : historicalTxns) {
			//dateTimeLocal = dateTimeLocal.minusDays(60);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)) {
				count++;
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
		}
		if (count > 0) {
			int txnAmount = currentTxn.getTxnAmt();
			if (txnAmount > (n_txn_amt / count)
					|| n_txn_amt > 20000)
				throw new FraudException(36);
		}

	}

	private void rule37()
			throws ParseException {
		int count = 0;
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime()).minusDays(70);
		for (Transaction txn : historicalTxns) {
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)) {
				count++;
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
		}

		if (count > 0) {
			if (currentTxn.getTxnAmt() > (n_txn_amt / count)
					|| n_txn_amt > 100000)
				throw new FraudException(37);
		}

	}

	private void rule38()
			throws ParseException {
		int count = 0;
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(70);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)) {
				count++;
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
		}

		if (count > 0) {
			if (currentTxn.getTxnAmt() > (n_txn_amt / count)
					|| n_txn_amt > 200000)
				throw new FraudException(38);
		}

	}

	private void rule39()
			throws ParseException {
		int count = 0;
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(90);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)) {
				count++;
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
		}

		if (count > 0) {
			if (currentTxn.getTxnAmt() > (n_txn_amt / count)
					|| n_txn_amt > 330000)
				throw new FraudException(39);
		}

	}

	private void rule40()
			throws ParseException {
		int count = 0;
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(90);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)) {
				count++;
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
		}

		if (count > 0) {
			if (currentTxn.getTxnAmt() > (n_txn_amt / count)
					|| n_txn_amt > 1110000)
				throw new FraudException(40);
		}

	}

	private void rule41()
			throws ParseException {
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(10);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal) && "001".equals(txn.getCountryCode())
					&& txn.getTxnAmt() > 10000
					&& "002".equals(currentTxn.getCountryCode())
					&& currentTxn.getTxnAmt() > 1000) {
				throw new FraudException(41);
			}
		}

	}

	private void rule42()
			throws ParseException {
		historicalTxns.remove(currentTxn);
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());

		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(10);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal) && (!"001".equals(txn.getCountryCode()))
					&& txn.getTxnAmt() > 10000
					&& "002".equals(currentTxn.getCountryCode())
					&& currentTxn.getTxnAmt() > 1000) {
				throw new FraudException(42);
			}
		}
		historicalTxns.add(currentTxn);
	}

	private void rule43()
			throws ParseException {

		if(historicalTxns.size() > 1) {
			DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
			if (historicalTxns.get(1).getTxnAmt() <= 100
					&& currentTxn.getTxnAmt() >= 10000) {
				dateTimeLocal = dateTimeLocal.minusMinutes(10);
				DateTime txn_time = new DateTime(currentTxn.getTimeStamp());
				if (txn_time.isAfter(dateTimeLocal)) {
					throw new FraudException(43);
				}
			}
		}
	}

	private void rule44()
			throws ParseException {

		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusHours(10);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)
					&& (2 > Integer.parseInt(txn.getCountryCode()) && currentTxn.getTxnAmt() > 10000)) {
				throw new FraudException(44);
			}
		}

	}

	private void rule45()
			throws ParseException {

		historicalTxns.remove(currentTxn);
		int count = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(1);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal) && "0022".equals(txn.getMerchantType())) {
				count++;
			}
			if (count > 10 && currentTxn.getTxnAmt() > 1000
					&& "0033".equals(currentTxn.getMerchantType())) {
				throw new FraudException(45);
			}
		}
		historicalTxns.add(currentTxn);
	}

	private void rule46() throws ParseException {

		historicalTxns.remove(currentTxn);
		int count = 0;
		for (Transaction txn : historicalTxns) {
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			int hourOfDay = txn_time.getHourOfDay();
			if (hourOfDay > 0 && hourOfDay < 4) {
				count++;
			}
			if (count > 50 && Integer.parseInt(txn.getTxnCode()) > 5
					&& currentTxn.getTxnAmt() > 20000) {
				throw new FraudException(46);
			}
		}
		historicalTxns.add(currentTxn);
	}

	private void rule47()
			throws ParseException {

		historicalTxns.remove(currentTxn);
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(80);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)
					&& ("200".equals(txn.getCountryCode()) || "201".equals(txn
							.getCountryCode()))) {
				dateTimeLocal = dateTimeLocal.plusDays(80);
				dateTimeLocal = dateTimeLocal.plusHours(1);
				DateTime dateTimeLocal2 = new DateTime(dateTimeLocal.toDateTime());
				if (txn_time.isAfter(dateTimeLocal2)) {
					n_txn_amt = n_txn_amt + txn.getTxnAmt();
				}
			}
		}
		if (n_txn_amt > 10000 && "122".equals(currentTxn.getCountryCode())) {
			throw new FraudException(47);
		}
		historicalTxns.add(currentTxn);
	}

	private void rule48()
			throws ParseException {

		historicalTxns.remove(currentTxn);
		int count = 0;

		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());

		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(1);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal) && "0022".equals(txn.getMerchantType())
					&& "0001".equals(txn.getTxnCode())) {
				count++;
			}
		}
		if (count > 10 && "0033".equals(currentTxn.getMerchantType())
				&& currentTxn.getTxnAmt() > 1000) {
			throw new FraudException(48);
		}
		historicalTxns.add(currentTxn);
	}

	private void rule49()
			throws ParseException {

		historicalTxns.remove(currentTxn);
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(10);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal) && "0022".equals(txn.getMerchantType())
					&& "0001".equals(txn.getTxnCode())) {
				n_txn_amt = n_txn_amt + txn.getTxnAmt();
			}
		}
		if (n_txn_amt > 10000 && "0033".equals(currentTxn.getMerchantType())
				&& currentTxn.getTxnAmt() > 10000) {
			throw new FraudException(49);
		}

		historicalTxns.add(currentTxn);
	}

	public boolean isFraudTxn() {
		return detectionResult;
	}
	public int getFailedTest() { return isFraudTxn() ? this.failedTest : 0;}

	public static class FraudException extends RuntimeException{
		private int test;
		public FraudException(int t){
			this.test = t;
		}

		public int getFailedTest() { return test;}
	}
}
