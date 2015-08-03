package com.hazelcast.certification.domain;

import java.io.Serializable;

/**
 * Object representation of incoming and historical transaction
 *
 */
public class Transaction implements Serializable {

	private static final long serialVersionUID = 7567524872106840493L;

	private String creditCardNumber;
	private long timeStamp;
	private String countryCode;
	private String responseCode;
	private String txnAmount;
	private String txnCurrency;
	private String txnCode = "";
	private String merchantType;
	private String txnCity;

	public String getCreditCardNumber() {
		return creditCardNumber;
	}

	public void setCreditCardNumber(String credit_card_number) {
		this.creditCardNumber = credit_card_number;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String country_code) {
		this.countryCode = country_code;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String response_code) {
		this.responseCode = response_code;
	}

	public String getTxnAmt() {
		return txnAmount;
	}

	public void setTxnAmt(String txnAmount) {
		this.txnAmount = txnAmount;
	}

	public String getTxnCurrency() {
		return txnCurrency;
	}

	public void setTxnCurrency(String txn_currency) {
		this.txnCurrency = txn_currency;
	}

	public String getTxnCode() {
		return txnCode;
	}

	public void setTxnCode(String txn_code) {
		this.txnCode = txn_code;
	}

	public String getMerchantType() {
		return merchantType;
	}

	public void setMerchantType(String merchant_type) {
		this.merchantType = merchant_type;
	}

	public String getTxnCity() {
		return txnCity;
	}

	public void setTxnCity(String txn_city) {
		this.txnCity = txn_city;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(creditCardNumber);
		sb.append(",");
		sb.append(timeStamp);
		sb.append(",");
		sb.append(countryCode);
		sb.append(",");
		sb.append(responseCode);
		sb.append(",");
		sb.append(txnAmount);
		sb.append(",");
		sb.append(countryCode);
		sb.append(",");
		sb.append(merchantType);
		sb.append(",");
		sb.append(txnCity);
		sb.append(",");
		sb.append(txnCode);
		
		return sb.toString();
	}
}
