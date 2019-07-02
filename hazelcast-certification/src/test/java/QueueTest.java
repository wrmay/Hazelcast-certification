import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.monitor.LocalQueueStats;

public class QueueTest {
    public static void main(String []args){
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        ILogger log = Logger.getLogger(QueueTest.class);

        IQueue<String> q = hz.getQueue("testq");
        String memberName = hz.getCluster().getLocalMember().toString();
        for(int i=0;i < 3; ++i){
            try {
                q.put(String.format("%s %03d", memberName, i));
            } catch(InterruptedException x){
                log.warning("Interrupted");
            }
        }

        final LocalQueueStats localQueueStats = q.getLocalQueueStats();

        while (true) {
            log.info(String.format("%s: owned items=%d  backup items=%d", memberName, localQueueStats.getOwnedItemCount(), localQueueStats.getBackupItemCount()));
            try {
                Thread.sleep(2000);
            } catch(InterruptedException x){

            }
        }

    }
}
