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
	private DateTime dateTime;

	public RuleEngine(Transaction currentTxn, List<Transaction> historicalTxns) {
		this.currentTxn = currentTxn;
		this.historicalTxns = historicalTxns;
		detectionResult = false;

		dateTime = new DateTime(currentTxn.getTimeStamp());
	}

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
		} catch (ParseException e) {
			log.severe(e);
		}
	}

	private void rule01() {
		if ("111".equals(currentTxn.getCountryCode())
				&& Integer.parseInt(currentTxn.getTxnAmt()) > 200000
				&& "0022".equals(currentTxn.getMerchantType()))
			detectionResult = true;
	}

	private void rule02() {
		if ("121".equals(currentTxn.getCountryCode())
				&& (!"00".equals(currentTxn.getResponseCode())))
			detectionResult = true;
	}

	private void rule03() {
		if ("0011".equals(currentTxn.getMerchantType())
				&& ((Integer.parseInt(currentTxn.getTxnAmt()) > 1000 && "011"
						.equals(currentTxn.getTxnCurrency())) || (Integer
						.parseInt(currentTxn.getTxnAmt()) > 1000 && "011"
						.equals(currentTxn.getTxnCurrency()))))
			detectionResult = true;
	}

	private void rule04() {
		if (Integer.parseInt(currentTxn.getTxnAmt()) > 100000
				&& "1000".equals(currentTxn.getTxnCode()))
			detectionResult = true;
	}

	private void rule05() {
		if (Integer.parseInt(currentTxn.getTxnAmt()) > 100000
				&& "00004000222100".equals(currentTxn.getCreditCardNumber()))
			detectionResult = true;
	}

	private void rule06() {
		if (("000112340000444422".equals(currentTxn.getCreditCardNumber()) || "000112340000444422"
				.equals(currentTxn.getCreditCardNumber()))
				&& "2000".equals(currentTxn.getMerchantType()))
			detectionResult = true;
	}

	private void rule07() {
		if (("0001".equals(currentTxn.getMerchantType()) || "2222"
				.equals(currentTxn.getMerchantType()))
				&& "221".equals(currentTxn.getCountryCode()))
			detectionResult = true;
	}

	private void rule08() {
		if ("3333".equals(currentTxn.getTxnCode())
				&& Integer.parseInt(currentTxn.getTxnAmt()) > 10000
				&& "2222".equals(currentTxn.getMerchantType()))
			detectionResult = true;
	}

	private void rule09() throws ParseException {
		if (dateTime.getHourOfDay() > 0 && dateTime.getHourOfDay() < 4
				&& Integer.parseInt(currentTxn.getTxnAmt()) >= 10000)
			detectionResult = true;
	}

	private void rule10() throws ParseException {
		if (dateTime.getHourOfDay() > 0 && dateTime.getHourOfDay() < 4
				&& "0101".equals(currentTxn.getMerchantType())
				&& Integer.parseInt(currentTxn.getTxnAmt()) >= 10000)
			detectionResult = true;
	}

	private void rule11() {
		if (("221".equals(currentTxn.getCountryCode()) || "444"
				.equals(currentTxn.getCountryCode()))
				&& ("0001".equals(currentTxn.getMerchantType()) || "2222"
						.equals(currentTxn.getMerchantType())))
			detectionResult = true;
	}

	private void rule12() {
		if ("00004000222333".equals(currentTxn.getCreditCardNumber())
				&& Integer.parseInt(currentTxn.getTxnAmt()) > 50000)
			detectionResult = true;
	}

	private void rule13() {
		if (("000112340000444444".equals(currentTxn.getCreditCardNumber()) || "003300030044442211"
				.equals(currentTxn.getCountryCode()))
				&& "2200".equals(currentTxn.getMerchantType()))
			detectionResult = true;
	}

	private void rule14() {
		if (("000112340000444444".equals(currentTxn.getCreditCardNumber()) || "003300030044442211"
				.equals(currentTxn.getCountryCode()))
				&& !"2222".equals(currentTxn.getTxnCode()))
			detectionResult = true;
	}

	private void rule15() throws ParseException {
		if (dateTime.getHourOfDay() > 0 && dateTime.getHourOfDay() < 4
				&& "2222".equals(currentTxn.getMerchantType())
				&& Integer.parseInt(currentTxn.getTxnAmt()) >= 10000)
			detectionResult = true;
	}

	private void rule16() throws ParseException {
		if (dateTime.getHourOfDay() > 0 && dateTime.getHourOfDay() < 4
				&& "2222".equals(currentTxn.getTxnCode())
				&& Integer.parseInt(currentTxn.getTxnAmt()) >= 10000)
			detectionResult = true;
	}

	private void rule17() throws ParseException {
		if (dateTime.getMonthOfYear() == 3 && dateTime.getDayOfMonth() == 25
				&& "2222".equals(currentTxn.getTxnCode())
				&& Integer.parseInt(currentTxn.getTxnAmt()) >= 10000)
			detectionResult = true;

	}

	private void rule18() throws ParseException {
		if (dateTime.getMonthOfYear() == 3 && dateTime.getDayOfMonth() == 25
				&& "2222".equals(currentTxn.getMerchantType())
				&& Integer.parseInt(currentTxn.getTxnAmt()) >= 10000)
			detectionResult = true;

	}

	private void rule19() throws ParseException {
		if ("3344".equals(currentTxn.getTxnCode())
				&& Integer.parseInt(currentTxn.getTxnAmt()) >= 1000000)
			detectionResult = true;

	}

	private void rule20() throws ParseException {
		if ("1001".equals(currentTxn.getTxnCode())
				&& Integer.parseInt(currentTxn.getTxnAmt()) >= 500000)
			detectionResult = true;

	}

	private void rule21() throws ParseException {
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		dateTimeLocal = dateTimeLocal.minusHours(1);
		int n_txn_amt = 0;
		for (Transaction txn : historicalTxns) {
			DateTime historicalDateTime = new DateTime(txn.getTimeStamp());
			if (historicalDateTime.isAfter(dateTimeLocal))
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
		}
		if ("00004000222333".equals(currentTxn.getCreditCardNumber())
				&& n_txn_amt > 500000)
			detectionResult = true;

	}

	private void rule22()
			throws ParseException {
		int n_txn_amt = 0;

		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		dateTimeLocal = dateTimeLocal.minusHours(5);
		for (Transaction txn : historicalTxns) {
			DateTime historicalDT = new DateTime(txn.getTimeStamp());
			if (historicalDT.isAfter(dateTimeLocal))
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
		}
		if ("3333".equals(currentTxn.getTxnCode()) && n_txn_amt > 1000000
				&& "2222".equals(currentTxn.getMerchantType()))
			detectionResult = true;

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
			detectionResult = true;

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
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (count > 10 && n_txn_amt >= 10000000)
			detectionResult = true;

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
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
			if (txn_time.isAfter(dateTimeLocal)
					&& "0011".equals(currentTxn.getMerchantType())
					&& ("022".equals(currentTxn.getTxnCurrency()))) {
				n_txn_amt1 = n_txn_amt1 + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (n_txn_amt > 10000 || n_txn_amt1 > 20000)
			detectionResult = true;

	}

	private void rule26()
			throws ParseException {
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(1);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)) {
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if ("00004000222333".equals(currentTxn.getCreditCardNumber())
				&& n_txn_amt >= 500000)
			detectionResult = true;

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
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (n_txn_amt >= 1000000)
			detectionResult = true;

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
			detectionResult = true;

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
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (count > 10 && n_txn_amt >= 10000000)
			detectionResult = true;

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
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
			if (txn_time.isAfter(dateTimeLocal)
					&& "0011".equals(currentTxn.getMerchantType())
					&& ("022".equals(currentTxn.getTxnCurrency()))) {
				n_txn_amt1 = n_txn_amt1 + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (n_txn_amt > 10000 || n_txn_amt1 > 20000)
			detectionResult = true;

	}

	private void rule31()
			throws ParseException {
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(10);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)) {
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if ("00004000222333".equals(currentTxn.getCreditCardNumber())
				&& n_txn_amt > 500000)
			detectionResult = true;

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
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (n_txn_amt > 500000)
			detectionResult = true;

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
			detectionResult = true;

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
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (count > 10 && n_txn_amt >= 10000000)
			detectionResult = true;

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
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
			if (txn_time.isAfter(dateTimeLocal)
					&& "0011".equals(currentTxn.getMerchantType())
					&& ("022".equals(currentTxn.getTxnCurrency()))) {
				n_txn_amt1 = n_txn_amt1 + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (n_txn_amt > 10000 || n_txn_amt1 > 20000)
			detectionResult = true;

	}

	private void rule36()
			throws ParseException {
		int count = 0;
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(60);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)) {
				count++;
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (Integer.parseInt(currentTxn.getTxnAmt()) > (n_txn_amt / count)
				|| n_txn_amt > 20000)
			detectionResult = true;

	}

	private void rule37()
			throws ParseException {
		int count = 0;
		int n_txn_amt = 0;
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(70);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal)) {
				count++;
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (Integer.parseInt(currentTxn.getTxnAmt()) > (n_txn_amt / count)
				|| n_txn_amt > 100000)
			detectionResult = true;

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
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (Integer.parseInt(currentTxn.getTxnAmt()) > (n_txn_amt / count)
				|| n_txn_amt > 200000)
			detectionResult = true;

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
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (Integer.parseInt(currentTxn.getTxnAmt()) > (n_txn_amt / count)
				|| n_txn_amt > 330000)
			detectionResult = true;

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
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (Integer.parseInt(currentTxn.getTxnAmt()) > (n_txn_amt / count)
				|| n_txn_amt > 1110000)
			detectionResult = true;

	}

	private void rule41()
			throws ParseException {
		DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
		for (Transaction txn : historicalTxns) {
			dateTimeLocal = dateTimeLocal.minusDays(10);
			DateTime txn_time = new DateTime(txn.getTimeStamp());
			if (txn_time.isAfter(dateTimeLocal) && "001".equals(txn.getCountryCode())
					&& Integer.parseInt(txn.getTxnAmt()) > 10000
					&& "002".equals(currentTxn.getCountryCode())
					&& Integer.parseInt(currentTxn.getTxnAmt()) > 1000) {
				detectionResult = true;
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
					&& Integer.parseInt(txn.getTxnAmt()) > 10000
					&& "002".equals(currentTxn.getCountryCode())
					&& Integer.parseInt(currentTxn.getTxnAmt()) > 1000) {
				detectionResult = true;
			}
		}
		historicalTxns.add(currentTxn);
	}

	private void rule43()
			throws ParseException {

		if(historicalTxns.size() > 1) {
			DateTime dateTimeLocal = new DateTime(dateTime.toDateTime());
			if (Integer.parseInt(historicalTxns.get(1).getTxnAmt()) <= 100
					&& Integer.parseInt(currentTxn.getTxnAmt()) >= 10000) {
				dateTimeLocal = dateTimeLocal.minusMinutes(10);
				DateTime txn_time = new DateTime(currentTxn.getTimeStamp());
				if (txn_time.isAfter(dateTimeLocal)) {
					detectionResult = true;
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
					&& (2 > Integer.parseInt(txn.getCountryCode()) && Integer
							.parseInt(currentTxn.getTxnAmt()) > 10000)) {
				detectionResult = true;
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
			if (count > 10 && Integer.parseInt(currentTxn.getTxnAmt()) > 1000
					&& "0033".equals(currentTxn.getMerchantType())) {
				detectionResult = true;
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
					&& Integer.parseInt(currentTxn.getTxnAmt()) > 20000) {
				detectionResult = true;
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
					n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
				}
			}
		}
		if (n_txn_amt > 10000 && "122".equals(currentTxn.getCountryCode())) {
			detectionResult = true;
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
				&& Integer.parseInt(currentTxn.getTxnAmt()) > 1000) {
			detectionResult = true;
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
				n_txn_amt = n_txn_amt + Integer.parseInt(txn.getTxnAmt());
			}
		}
		if (n_txn_amt > 10000 && "0033".equals(currentTxn.getMerchantType())
				&& Integer.parseInt(currentTxn.getTxnAmt()) > 10000) {
			detectionResult = true;
		}

		historicalTxns.add(currentTxn);
	}

	public boolean isFraudTxn() {
		return detectionResult;
	}
}
