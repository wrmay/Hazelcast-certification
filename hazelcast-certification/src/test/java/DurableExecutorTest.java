
// what happens when the executor queue becomes full ?

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.durableexecutor.DurableExecutorService;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.osgi.HazelcastOSGiInstance;

import java.io.Serializable;

// Sample Output

//        Jul 01, 2019 10:36:26 AM DurableExecutorTest
//        INFO: FINISHED task 0
//        Jul 01, 2019 10:36:30 AM com.hazelcast.spi.impl.operationservice.impl.InvocationMonitor
//        INFO: [localhost]:5702 [dev] [3.12] Invocations:1 timeouts:0 backup-timeouts:1
//        Jul 01, 2019 10:36:30 AM DurableExecutorTest
//        INFO: SUBMITTED 0
//        Jul 01, 2019 10:36:32 AM DurableExecutorTest
//        INFO: FINISHED task 1
//        Jul 01, 2019 10:36:36 AM com.hazelcast.spi.impl.operationservice.impl.InvocationMonitor
//        INFO: [localhost]:5702 [dev] [3.12] Invocations:1 timeouts:0 backup-timeouts:1
//        Jul 01, 2019 10:36:36 AM DurableExecutorTest
//        INFO: SUBMITTED 1
//        Jul 01, 2019 10:36:38 AM DurableExecutorTest
//        INFO: FINISHED task 2
//        Jul 01, 2019 10:36:42 AM com.hazelcast.spi.impl.operationservice.impl.InvocationMonitor
//        INFO: [localhost]:5702 [dev] [3.12] Invocations:1 timeouts:0 backup-timeouts:1
//        Jul 01, 2019 10:36:42 AM DurableExecutorTest
//        INFO: SUBMITTED 2
//        Jul 01, 2019 10:36:42 AM DurableExecutorTest
//        INFO: SUBMITTED 3
//        Jul 01, 2019 10:36:42 AM DurableExecutorTest
//        INFO: SUBMITTED 4
//        Jul 01, 2019 10:36:42 AM DurableExecutorTest
//        INFO: SUBMITTED 5
//        Jul 01, 2019 10:36:44 AM DurableExecutorTest
//        INFO: FINISHED task 6
//        Jul 01, 2019 10:36:48 AM com.hazelcast.spi.impl.operationservice.impl.InvocationMonitor
//        INFO: [localhost]:5702 [dev] [3.12] Invocations:1 timeouts:0 backup-timeouts:1
//        Jul 01, 2019 10:36:48 AM DurableExecutorTest
//        INFO: SUBMITTED 6
//        Jul 01, 2019 10:36:50 AM DurableExecutorTest
//        INFO: FINISHED task 7
//        Jul 01, 2019 10:36:54 AM com.hazelcast.spi.impl.operationservice.impl.InvocationMonitor
//        INFO: [localhost]:5702 [dev] [3.12] Invocations:1 timeouts:0 backup-timeouts:1
//        Jul 01, 2019 10:36:54 AM DurableExecutorTest
//        INFO: SUBMITTED 7
//        Jul 01, 2019 10:36:54 AM DurableExecutorTest
//        INFO: SUBMITTED 8
//        Jul 01, 2019 10:36:56 AM DurableExecutorTest
//        INFO: FINISHED task 9
//        Jul 01, 2019 10:37:00 AM com.hazelcast.spi.impl.operationservice.impl.InvocationMonitor
//        INFO: [localhost]:5702 [dev] [3.12] Invocations:1 timeouts:0 backup-timeouts:1
//        Jul 01, 2019 10:37:00 AM DurableExecutorTest
//        INFO: SUBMITTED 9
//        Jul 01, 2019 10:37:02 AM DurableExecutorTest
//        INFO: FINISHED task 10
//        Jul 01, 2019 10:37:06 AM com.hazelcast.spi.impl.operationservice.impl.InvocationMonitor
//        INFO: [localhost]:5702 [dev] [3.12] Invocations:1 timeouts:0 backup-timeouts:1
//        Jul 01, 2019 10:37:06 AM DurableExecutorTest
//        INFO: SUBMITTED 10
//        Jul 01, 2019 10:37:06 AM DurableExecutorTest
//        INFO: SUBMITTED 11


// LESSONS LEARNED
//
// DurableExecutor.submit actually waits for the task to be done
// Items that are not processed are silently dropped !


public class DurableExecutorTest {

    private static final ILogger log = Logger.getLogger(DurableExecutorTest.class);

    public static void main(String []args){
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        DurableExecutorService executor = hz.getDurableExecutorService("test");

        // the executor has a capacity of 10 so submit 12 tasks to make it overflow
        for(int i=0; i<12; ++i){
            executor.submitToKeyOwner(new SlowTask(i), i);
            log.info("SUBMITTED " + i);
        }

        hz.shutdown();
    }


    private static class SlowTask implements Serializable, Runnable {

        private int i;

        public SlowTask(int i){
            this.i = i;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                log.info("FINISHED task " + i);
            } catch(InterruptedException x){
                //
            }
        }
    }
}
