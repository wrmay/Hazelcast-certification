package com.hazelcast.certification.process;

import com.hazelcast.certification.business.ruleengine.RuleEngine;
import com.hazelcast.certification.domain.Result;
import com.hazelcast.certification.domain.Transaction;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleFraudDetection extends FraudDetection  {

    private final static ILogger log = Logger.getLogger(SimpleFraudDetection.class);

    private Map<String, List<Transaction>> cardHistory  = new HashMap<String, List<Transaction>>(30000000);


    // this thing is not thread safe - which is OK for now
    protected void handle(Transaction t) {
        List<Transaction> txnList = cardHistory.get(t.getCreditCardNumber());
        if (txnList == null){
            txnList = new ArrayList<Transaction>(100);
            cardHistory.put(t.getCreditCardNumber(), txnList);
        }

        // should at least use a pool so we don't keep newing this over and over
        RuleEngine re = new RuleEngine(t, txnList);
        re.executeRules();
        registerResult(new Result(t.getCreditCardNumber(), re.isFraudTxn()));

        txnList.add(t);
    }


}
