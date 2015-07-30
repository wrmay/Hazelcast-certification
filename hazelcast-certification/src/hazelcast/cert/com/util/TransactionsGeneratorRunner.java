package hazelcast.cert.com.util;


public class TransactionsGeneratorRunner {
	public static void main(String arg[]) {
		new Thread(new TransactionsGenerator()).start();
	}
}
