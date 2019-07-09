import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import java.util.Map;

public class EPTest {
    private static final ILogger log = Logger.getLogger(EPTest.class);

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        IMap<Integer, Message> map = hz.getMap("eptest");
        map.set(1, new Message("HELLO"));
        map.executeOnKey(1, new HelloWorldEntryProcessor());
        Message m = map.get(1);
        log.info(m.toString());
        hz.shutdown();
    }

    public static class HelloWorldEntryProcessor implements EntryProcessor<Integer, Message>, EntryBackupProcessor<Integer, Message> {

        @Override
        public Object process(Map.Entry<Integer, Message> entry) {
            Message m = entry.getValue();
            m.setMessage("HELLO WORLD!");
            entry.setValue(m);   // not actually necessary when the map uses Object in-memory format
            return null;
        }

        @Override
        public EntryBackupProcessor<Integer, Message> getBackupProcessor() {
            return this;
        }

        @Override
        public void processBackup(Map.Entry<Integer, Message> entry) {
            process(entry);
        }
    }
}
