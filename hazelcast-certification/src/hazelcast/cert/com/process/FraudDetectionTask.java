package hazelcast.cert.com.process;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import hazelcast.cert.com.business.ruleengine.RuleEngine;
import hazelcast.cert.com.data.DataAccessManager;
import hazelcast.cert.com.domain.Transaction;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

public class FraudDetectionTask implements Callable<Boolean>, Serializable {
	
	private final static ILogger log = Logger.getLogger(FraudDetectionTask.class);
	
	private static final long serialVersionUID = 1L;
	private Transaction txn;

	FraudDetectionTask(Transaction txn) {
		this.txn = txn;
	}
	
	@Override
	public Boolean call() throws Exception {
		Object obj = Class.forName("hazelcast.cert.com.data.DataAccessManagerImpl").newInstance();
		if(obj == null || !(obj instanceof DataAccessManager)) {
			log.severe("Invalid DataAccessManager. Provider must implement hazelcast.cert.com.data.DataAccessManager.");
			System.exit(0);
		}
		DataAccessManager dm = (DataAccessManager) obj;
		List<Transaction> allTxns = dm.updateAndGet(txn);
		
		RuleEngine ruleEngine = new RuleEngine();
		ruleEngine.setRulesAttributes(txn, allTxns);
		ruleEngine.executeRules();
		return ruleEngine.isFraudTxn();
	}
}
