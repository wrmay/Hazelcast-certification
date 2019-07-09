package com.hazelcast.certification.domain;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.io.Serializable;

/**
 * Object representation of incoming and historical transaction
 *
 */
public class Transaction implements DataSerializable, Serializable {

	private long timeStamp;
	private int txnAmount;
	private byte []stringFields;
	private FraudCheck fraudCheck;

	public Transaction() {
		stringFields = new byte[36];
	}

	public String getCreditCardNumber() {
		return new String(stringFields, 0, 14);
	}

	public void setCreditCardNumber(String credit_card_number) {
		System.arraycopy(credit_card_number.getBytes(), 0, stringFields, 0, 14);
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getCountryCode() {
		return new String(stringFields, 14, 3);
	};

	public void setCountryCode(String country_code) {
		System.arraycopy(country_code.getBytes(), 0, stringFields, 14, 3);
	}

	public String getResponseCode() {
		return new String(stringFields, 17, 2);
	}

	public void setResponseCode(String response_code) {
		System.arraycopy(response_code.getBytes(), 0, stringFields, 17, 2);
	}

	public int getTxnAmt() {
		return txnAmount;
	}

	public void setTxnAmt(int txnAmount) {
		this.txnAmount = txnAmount;
	}

	public String getTxnCurrency() {
		return new String(stringFields, 33, 3);
	}

	public void setTxnCurrency(String txn_currency) {
		System.arraycopy(txn_currency.getBytes(), 0, stringFields, 33, 3);
	}

	public String getTxnCode() {
		return new String(stringFields, 28, 5);
	}

	public void setTxnCode(String txn_code) {
		System.arraycopy(txn_code.getBytes(),0,stringFields,28,5);
	}

	public String getMerchantType() {
		return new String(stringFields, 19,4);
	}

	public void setMerchantType(String merchant_type) {
		System.arraycopy(merchant_type.getBytes(), 0, stringFields, 19, 4);
	}

	public String getTxnCity() {
		return new String(stringFields, 23, 5);
	}

	public void setTxnCity(String txn_city) {
		System.arraycopy(txn_city.getBytes(), 0, stringFields, 23, 5);
	}

	public FraudCheck getFraudCheck() {
		return fraudCheck;
	}

	public void setFraudCheck(FraudCheck fraudCheck) {
		this.fraudCheck = fraudCheck;
	}

//	public String toString() {
//		StringBuffer sb = new StringBuffer();
//		sb.append(creditCardNumber);
//		sb.append(",");
//		sb.append(timeStamp);
//		sb.append(",");
//		sb.append(countryCode);
//		sb.append(",");
//		sb.append(responseCode);
//		sb.append(",");
//		sb.append(txnAmount);
//		sb.append(",");
//		sb.append(countryCode);
//		sb.append(",");
//		sb.append(merchantType);
//		sb.append(",");
//		sb.append(txnCity);
//		sb.append(",");
//		sb.append(txnCode);
//
//		return sb.toString();
//	}

	public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
		objectDataOutput.writeLong(timeStamp);
		objectDataOutput.writeInt(txnAmount);
		objectDataOutput.writeObject(fraudCheck);
		objectDataOutput.writeByteArray(stringFields);
	}

	public void readData(ObjectDataInput objectDataInput) throws IOException {
		timeStamp = objectDataInput.readLong();
		txnAmount = objectDataInput.readInt();
		fraudCheck = objectDataInput.readObject();
		stringFields = objectDataInput.readByteArray();
	}
}
