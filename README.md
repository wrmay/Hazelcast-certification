# Contents

This document describes the primary findings.  Many tests were run on AWS infrastructure.  To faciliate rapid test cycles, reusable machinery was developed.  It should be possible for anyone to quickly duplicate these results using the same machinery.  For instructions on that, see [USAGE.md](USAGE.md).

The raw details of each test run can be found in [TEST_RESULTS_ROUND_1_3.md](TEST_RESULTS_ROUND_1_3.md) and [TEST_RESULTS_ROUND_4_N.md](TEST_RESULTS_ROUND_4_N.md)



Various related side inquiries were documented [here](INVESTIGATIONS.md) and [here](https://github.com/wrmay/threads-and-events).

# Certification Problem Overview

The problem is to build a system that scores credit card transactions using the provided rule engine and transaction generation code.  

_Based on the [original project](https://github.com/hazelcast/hazelcast-certification), the challenge has been implemented  as a streaming" system, not a request-response system._ In other words, there is no "scoring service".  Instead, as in the original solution, transactions are pulled from a transaction source, evaluated for fraud and the result is recorded within the system.

The following additional requirements are also understood:

- The solution must be fault tolerant.  This is of course the source of much difficulty.  The original system is very fast and efficient but it does not back up its data anywhere so it is not fault tolerant.
- The solution must be scalable both in terms of the number of credit cards in the system and the capacity to score transactions.  Again, the original solution is restricted to one machine and does not have these qualities.

# Solution Architecture

![architecture](images/architecture.png)

1. The central data structure is the "transaction_history" map.  The key is credit card number and the value is a LinkedList<Transaction> comprising the credit card's recent transaction history.  The [original transaction generator](hazelcast-certification/src/main/java/com/hazelcast/certification/util/TransactionsUtil.java) has been incorporated into a [map loader](hazelcast-certification/src/main/java/com/hazelcast/certification/util/TransactionMapLoader.java).  This is used to load the history in a distributed manner.  30 million credit cards with 20 historical transactions each takes a few minutes to load using this approach.
2. Each [hazelcast member](hazelcast-certification/src/main/java/com/hazelcast/certification/server/FraudDetectionServer.java) is also running a configurable number of [transaction reader threads](hazelcast-certification/src/main/java/com/hazelcast/certification/server/TransactionSource.java)that pull a transaction from the [transaction source](hazelcast-certification/src/main/java/com/hazelcast/certification/util/TransactionsGenerator.java), ascertains the credit card number, and invokes an [entry processor](hazelcast-certification/src/main/java/com/hazelcast/certification/server/ProcessTransactionEntryProcessor.java) on the appropriate key.
3. The  [entry processor](hazelcast-certification/src/main/java/com/hazelcast/certification/server/ProcessTransactionEntryProcessor.java) does the following.  Obtain the transaction history from the "transaction_history" map. Execute the rule engine.  Record the result (fraudulent or not) back onto the transaction along with a timestamp and the rule that it failed on.  The newest transaction is added to the list and the oldest is removed.
4. To ascertain the throughput, originally, a [fast aggregator](hazelcast-certification/src/main/java/com/hazelcast/certification/util/StatsAggregator.java) was used.  It scans all transaction histories in reverse order, accumulating the number of transactions scored and stopping when it reaches a transaction that was scored more than 10 seconds ago.  On the full 30 million cards (each with 20 historical transactions) this runs in a few seconds with no obvious impact on CPU. Later, a custom Prometheus metric as added which is incremented inside of the entry processor.  Results from both approaches agreed but the Prometheus metric allows the throughput to be graphed over time.

__Additional Optimization and Notes__

- The most important architectural priority was to use Entry Processors.  The main idea is to have the workload run within entry processors because of the following benefits:  
  - all data access is local
  - even the data that changes does not have to be replicated thanks to backup entry processors.
  - the work will automatically leverage the data colocated work queues and thread pools built into the Hazelcast partition threading model.  The number of independent worker threads can be set directly using the "hazelcast.operation.thread.count" property.
- The data in the transaction history is stored in OBJECT format to avoid repeated serialization / deserialization of the large transaction history lists.  HD memory was not used because  the cost of deserializing the whole transaction list to process one transaction was assumed to be too high.  Also, in the current solution there is no sign of GC stress.
- Use of Entry Processors precluded storing the data in a MultiMap, something which I otherwise would have considered.
- _The transaction history is kept to a constant length._  All of the transactions generated by the transaction generator are in the last 90 days.  Although it would not be a problem in a real system, in this exercise, if the 90 day guideline were strictly followed it would result in unbounded growth of the list of transactions that must be evaluated to score a transaction. _Growth from new customers can be handled by adding capacity  but having the work required to score one transaction grow unbounded would probably not be acceptable to anyone._ To address this issue,  as new transactions are added to the end, old transactions are removed from the beginning.  Fixing the size of the history is necessary to avoid having the work required to score a card grow in a completely unbounded fashion.  
- Transactions are kept in String format as long as possible to minimize additional serialization and deserialization.  Specifically, tranactions are sent to the entry processor as Strings not Transaciton objects.
- The representation of the [Transaction](https://github.com/wrmay/Hazelcast-certification/blob/master/hazelcast-certification/src/main/java/com/hazelcast/certification/domain/Transaction.java) is optimized.  Storing a lot of short strings is very inefficient.  Even an empty java String can consume 15 bytes.  Since the transactions in this system number in the hundreds of millions, some special attention for Transactions is justified.  All of the String fields (which are fixed width) are combined into a single byte array.  Getters and setters are used to pack and unpack the appropriate portion of the byte array.  [Tests](hazelcast-certification/src/test/java/TransactionTest.java) were added to ensure that this scheme actually worked correctly.  In the process, some flaws in the provided transaction generation code were discovered and corrected.  _This approach reduced the memory requirement for 2 copies of transaction data from 32G per million transactions to 8G per million_.  Note that these numbers were obtained by observing the overall memory usage of a loaded and running JVM.  They include the memory for storing transactions but they also include uncollected garbage, JVM working space and other overhead.  In other words, the numbers cannot necessarily be used to compute the in memory size of a transaction (only an upper bound).
- The [rule engine](hazelcast-certification/src/main/java/com/hazelcast/certification/business/ruleengine/RuleEngine.java) has been modified with "short circuiting" logic.  Once a transaction is determined to be fraudulent based on one rule, the subsequent rules are not executed since they could not change the outcome.
- The throughput of the system was very sensitive to  the balance of partition threads (hazelcast.operation.thread.count) vs. transaction reader threads.  Trial and error was required to find a good balance.

- Also, here are the statistics on history map during a run.

  ![map stats](images/map_stats.png)

  _Note there are no gets!_. This is because we are using entry processors and aggregators  to send the processing to the data.  That data does not need to be fetched by anyone.

__Instrumentation__

_Prometheus_ was used for instrumentation.  Using the _Prometheus_ java client, a custom counter was exposed to count transactions processed.  Note that care was taken to count only the execution of the primary entry processor and to exclude execution of the backup entry processor.

JVM and operating system metrics are exposed via the _Prometheus_ JMX exporter.  

All metrics are collected into a central _Prometheus_ server running on the same machine as the Hazelcast Management Server.

__Provisioning and Configuration__

Provisioning of the servers on AWS is automated using [cloudlab](https://github.com/wrmay/cloudlab), a tool written by the author.  Installation and configuration of Hazelcast IMDG and Prometheus is performed via an Ansible [playbook](cloudlab/setup.yaml).  All processes are daemonized via [supervisord](http://supervisord.org).



# Infrastructure

__Volume:__ 30 million cards, each with 20  transactions.

__Infrastructure__:

-  (rounds 1-3) m5.2xlarge instances running on AWS.  Each has 32G RAM and 8 vcpus (4 real cores, 8 hyperthreads)
- (rounds 4+) r5.2x large instances. 64G RAM and 8 vcpu (same CPU as above but  2x memory)

__JVM Settings__: 

- (rounds 1-3) -Xms28g -Xmx28g -Xmn4g -XX:+UseParNewGC -XX:+UseConcMarkSweepGC (note that CompressedOOPs is enabled by default).

- (round 4) -Xms60g -Xmx60g -Xmn6g -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark

- (round 5) -Xmx8g -Xms8g -Xmn4g

  

# Results 

The table below shows the results of testing several alternatives at different scales.  The units are all thousands of transactions per second.  Detailed results, throughput, CPU and heap utiulization for each  run can be found in [a separate document](TEST_RESULTS.md).	

| Nodes | 1    | 1a   | 1b   | 2    | X    | 3    | 4       | 5    |
| ----- | ---- | ---- | ---- | ---- | ---- | ---- | ------- | ---- |
| 6     |      |      |      |      |      |      | 183     | 136  |
| 8     | 102  |      |      | 138  | 97   | 193  |         |      |
| 10    | 113  |      |      |      |      |      |         |      |
| 12    | 124  |      |      | 156  | 115  | 267  | 450     | 227  |
| 14    | 116  |      |      |      |      |      |         |      |
| 16    | 138  | 140  | 153  | 166  | 141  | 323  |         |      |
| 18    |      |      |      |      |      |      | **670** | 286  |
| 20    |      |      |      | 170  | 161  |      |         |      |

Round 1: Entry processor based solution with OBJECT serialization

Round 1a: Increase reader threads to 128, Partitions to 1031

Round 1b: Increase reader threads to 192, Partitions to 2063

Round 2: Round 1 + Implement Entry Processor Pipelining 

Round X: Round 1 + Network Batching 

Round 3: Round 2 + Network Batching

Round 4: Round 3 + 90 day enforcement and custom container for transaction history

Round 5: Round 4 + HD



The initial series of tests yielded fairly good results with 138 TPS on 16 servers.  However, this architecture showed  scaling problems and it did not use all of the available CPU.  The chart below shows the throughput measured on the base architecture runs fitted with a quadratic trend line (see also the [results spreadsheet](results.xlsx)).  As can be seen, the trend line flattens out at around 16 servers so no further benefit would be expected from adding servers.

![base_tput](base.png)



This chart shows the CPU utilization during the 16 node test run.

![base_16_cpu](images/16/cpu.png)

Since most of the work is done on partition threads, dramatically increasing both the number of partitions and the number of partition threads was tried.  A [side investigation](https://github.com/wrmay/threads-and-events) was also conducted which indicated that the cost of thread context switching on a JVM is quite low.   This yielded a throughput of 153k TPS but CPU utilization did not increase dramatically indicating some other limiting factor.  

__Round 2: add EntryProcessor Pipelining__

Each transaction is scored by invoking an entry processor using the `IMap.executeOnKey` method.  While the entry is waiting to be processed, the caller thread can do nothing but wait.  Ideally it would do some useful work like reading the next transaction. In theory, this could be addressed by simply increasing the number of threads.  The idea being that while one thread is waiting the JVM will select and execute another thread. However, as can be seen in the 3rd and 4th columns in the table above, the results of this approach were not dramatic.

To address this, the architecture was revised to perform "entry processor pipelining".  Instead of each transaction reader thread synchronously invoking an entry processor, the transaction readers were modified to call "submitToKey" with a callback.  A (non-distributed) blocking queue of finite capacity was used limit the number of transactions that could be "in flight".  See the code blocks below.

BEFORE

```java
    private void process() {
        String rawTxnString = new String(buffer, encoding);
        int z = rawTxnString.indexOf(0);
        String txnString = rawTxnString.substring(0, z);
        int i = txnString.indexOf(",");
        String ccNumber = txnString.substring(0, i);
        txnHistory.executeOnKey(ccNumber, new ProcessTransactionEntryProcessor(txnString));
    }

```

AFTER

```java
 private void process() {
        String rawTxnString = new String(buffer, encoding);
        int z = rawTxnString.indexOf(0);
        String txnString = rawTxnString.substring(0, z);
        int i = txnString.indexOf(",");
        String ccNumber = txnString.substring(0, i);
        try {
            inFlightTransactions.put(txnString);  // blocks to provide back pressure
        } catch(InterruptedException ix){
            log.severe("Unexpected Exception");
        }
        txnHistory.submitToKey(ccNumber,new ProcessTransactionEntryProcessor(txnString), new EntryStoredCallback(txnString));
        transactionsSubmitted.inc();
    }
```

Also, two transaction servers were used for the 12 and 16 node runs, and 3 for the 20 node run.  This was done to eliminate the concern that the single threaded transaction servers would max out.

At 8 servers, this architecture matched the base architecture throughput of 138k TPS with 16 servers.  However, at higher scale, only a modest improvement was observed  with 166k TPS on 16 nodes and 170k TPS on 20 nodes. Note that the last 4 servers only increased throughput by 4K TPS.

![ep pipelining](ep_pipelining.png)





__Round 3: add network batching__

The results up to this point seem to  indicate some limiting resource.  The problem itself is "embarrasingly parallel" and the architecure is set up to exploit it.  The author was very confident that there were no fundamental architectural flaws.  The only other shared resources seem to be the transaction generator itself and the network.  The possibility of the single threaded transactions server becoming a limiting factor was mitigated (even in round 2) by running multiple.  

`iperf3` was used to test network throughput which tested above 10 Mbits/second or about 1.2G bytes/second.  The image below shows the AWS metrics for one of the servers during a high load test run.

![aws metrics](investigations/multiple_transaction_sources/aws_metrics_for_114.png)

It was noticed that, while utilization was high, at roughly .7 G bytes/second, it was not necessarily at capacity.  The other interesting thing is that, comparing the network throughput in bytes and in packets, one can see that _each packet is only about 230 bytes_.  At this size, a large portion of the packet is consumed with packet headers.

The way that the application used the network was then scrutinized. The reason for the small packets is as follows.   Each [transaction reader thread](hazelcast-certification/src/main/java/com/hazelcast/certification/server/TransactionSource.java) reads 100 bytes (the size of one transaction) and then processes it using an entry processor.  The network stack will fill the socket buffer and then 100 bytes will be consumed, leaving 100 bytes room in the buffer.  Unless the bytes are consumed as fast as they can be delivered, the consumer will send a 100 byte TCP window and the sender will "throttle back" accordingly.  This means that the sender will only send 100 bytes before waiting for an acknowledgement.  The network round trip time was measure at around .15ms (see [INVESTIGATIONS.md](INVESTIGATIONS.md)) but 100 bytes / .15ms = 100 / .00015 = 667k bytes a second on each socket and also 6.7k  transactions per second on each reader thread.  Even though the small TCP window is bad, the math here does not suggest that the TCP window is the direct cause but the small packets are a problem for another reason.  

Getting back to the shared resource hypothesis, _it is theorized that the infrastructure effectively has a speed limit in terms of packets/second (not bytes/second)_ .  Examples of potential sources include a firewall that has to examine each packet or intentional packets/second rate limiting enforced by the AWS infrastructure.  _On this theory, the transaction reader was modified to pull 100 transactions worth of bytes at a time._  This allows the network to send more than 100 bytes in each packet.  With 100x more data being sent in each packet, any "packets per second" speed limit will be avoided or at least deferred.  The source code for this modification is below (compare to the same method above - the only addition is the "for"  loop).

```java
    private void process() {
        for (int offset = 0;  (offset + TRANSACTION_SIZE) <= BUFFER_SIZE; offset += TRANSACTION_SIZE){
            String rawTxnString = new String(buffer,offset, TRANSACTION_SIZE,encoding);
            int z = rawTxnString.indexOf(0);
            String txnString = rawTxnString.substring(0, z);
            int i = txnString.indexOf(",");
            String ccNumber = txnString.substring(0, i);
            try {
                inFlightTransactions.put(txnString);  // blocks to provide back pressure
            } catch(InterruptedException ix){
                log.severe("Unexpected Exception");
            }
            txnHistory.submitToKey(ccNumber,new ProcessTransactionEntryProcessor(txnString), new EntryStoredCallback(txnString));
            transactionsSubmitted.inc();
        }
    }

```

The results were dramatic!

![best](best.png)



_Throughput went all the way to 193k TPS on 8 servers, adding 4 more servers gains 69K TPS and the next 4 adds 56K TPS for a high TPS of 323K TPS on 16 servers.  Although this is not linear, it is certainly much closer.  Furthermore, the problem has become CPU bound, a clear indicator that there had been a network related bottleneck before._

![cpu bound](images/pipeline_netbatch_16/cpu.png)



__Round 4: add 90 day history limit and improved history data structure __

The following changes were made for this round:

- The EC2 instance type was changed to r5.2x large allowing double the amount of data to be processed for a given number of cores.  Note the number of cores did not change.
- The code was changed to implement a strict 90 day history limit. 
- As part of the above effort the LinkedList containing transaction history was replaced with a custom data structure that was better for the task. The [TransactionHistoryContainer](hazelcast-certification/src/main/java/com/hazelcast/certification/util/TransactionHistoryContainer.java) is based on a singly linked list, which makes it more compact and more efficient for this application. [Tests](hazelcast-certification/src/test/java/com/TransactionHistoryContainerTest.java) to ensure correctness were run.
- IdentifiedDataSerializable was implemented for the transaction history data structures.
- Some [ JVM tuning](#Infrastructure) was applied.

Note that regular eviction was not an option in this case because each entry contains all history for one credit card.  The entry has to stay in memory but the history has to be truncated.  The TransactionHistoryContainer prunes the history whenever it is traversed (i.e. continuously).

Tests were conducted with 6, 12 and 18 nodes. The new, more efficient history data structure resulted in an increase in performance despite the addition of "90 day logic". 

The observations are plotted on a graph below.  For 6 server,  183kTPS / 6 servers = 30.5 kTPS / server.  For 12 servers, 450 kTPS / 12 servers = 37.5 kTPS / server. For 18 servers, 670k TPS / 18 = 37.2k TPS / server.  In other words, the 12 and 18 server tests showed very close to linear scaling.  In the 6 server test, each server did not deliver as many TPS per server.  This was probably due to some GC pressure.

![90day](images/round4_scaling.png)



For detailed results, see [TEST_RESULTS_ROUND_4_N.md](TEST_RESULTS_ROUND_4_N.md). This series of experiments is under the heading "Base Architecture + EP Pipelining + Network Level Batching + Strict 90 day Enforcement (a with clean as you go)"



__Round 5: add HD__

HD was added to the previous solution. After some experimentation to see what was the most stable, 52G were allocated to native memory with default settings and 8G was allocated to the JVM.

```xml
    <native-memory allocator-type="POOLED" enabled="true">
      <size unit="GIGABYTES" value="52"/>
      <min-block-size>16</min-block-size>
      <page-size>4194304</page-size>
      <metadata-space-percentage>12.5</metadata-space-percentage>
    </native-memory>

    <serialization>
      <data-serializable-factories>
        <data-serializable-factory factory-id="1">com.hazelcast.certification.domain.TransactionDataSerializableFactory</data-serializable-factory>
      </data-serializable-factories>
    </serialization>

    <map name="transaction_history">
        <in-memory-format>NATIVE</in-memory-format>
        <map-store enabled="true" initial-mode="EAGER">
            <class-name>com.hazelcast.certification.util.TransactionMapLoader</class-name>
            <properties>
                <property name="preload.cardCount">30000000</property>
                <property name="preload.txnCount">20</property>
            </properties>
        </map-store>
        <statistics-enabled>true</statistics-enabled>
    </map>
```



The JVM GC settings were `-Xmx8g -Xms8g -Xmn4g` .  This showed the best stability.  The large young gen is configured because the JVM no longer needs to deal with much longer lived data.  The vast majority of the allocations in the JVM will be very short lived so making a larger young gen avoids tenuring of short lived objects and the Old Gen GC that comes with it.  

The throughput results are charted below.

![hd_scaling](images/hd_scaling.png)



For detailed results, see [TEST_RESULTS_ROUND_4_N.md](TEST_RESULTS_ROUND_4_N.md). This series of experiments is under the heading "Base Architecture + EP Pipelining + Network Level Batching + Strict 90 day Enforcement (a with clean as you go) + HD"

Unfortunately, a long term downward trend in throughput was observed even though off-heap usage remained under 70% on all machines.  Fragmentation is my only suspect at present.  I will be pursuing this.

![hd throughput](images/pipeline_netbatch_90day_HD_18a_Extended/throughput.png)



![mancenter](images/pipeline_netbatch_90day_HD_18a_Extended/mancenter.png)





# A Note About Fault Tolerance

All of the transaction history is backed up because the Hazelcast map replicates data to a backup by default.  This means that transaction processing will continue, and all history will continue to be available, even if there is a failure.

However,  if a server is lost, transactions that have been read from the source and not yet processed will be lost.  With the base line architecture, each reader thread can have 1 transaction in flight.  With the later architectures, many more transactions can be in flight.  

_This is unavoidable with the problem as posed._  The cluster members must pull the transactions from the transaction source and even saving the transactions into a replicated data structure before processing would not solve the problem.  This is because the transaction generator uses a simple socket write to send the data and does not wait for any sort of application level ack. The fact that a socket write succeeds in the transaction generator does not mean that the consumer has processed, or even seen the transaction.  This is a somewhat misunderstood characteristic of TCP. See for example: https://www.stratus.com/openvos-blog/it-myths-tcp-guarantees-delivery-of-your-data/.

The two ways to handle this would be to change the submission mechanism to introduce application level acks or to make the source  "replayable".  Replayability is the approach implemented by Kafka and other Enterprise event streaming solutions.  Also, it is worth noting that source replayability is a requirement for reliable event processing in Jet. 

Of course the hazelcast client and server communication is reliable and incorporates application level acks so the whole end to interaction can be made reliable by replacing the "raw socket" communication with Hazelcast client-server communication.



On the "RR" (for request/response) branch of this repository is a sample implementation.  Instead of writing to a socket, the "FraudDetectionClient" class wraps a HazelcastClient which interacts with the cluster  via the Hazelcast APIs.  In order to achieve higher throughput, the FraudDetectionClient was implemented with pipelining and back pressure to throttle back the request rate when there are too many requests in flight.   The client was instrumented with counters for submitted requests, failed request, successful requests and slow requests. Slow requests are requests that take over 1 second to satisfy.  The results are below.

Throughput was in the 150K- 200K TPS range. 

![throughput](images/pipeline_netbatch_90day_RR_6/throughput.png)



There were comparatively few slow transactions. Typically less than .03 percent (NOT 3 percent) of the transactions took over 1 second.  None timed out.

![throughput](images/pipeline_netbatch_90day_RR_6/slow_transaction_rate.png)




