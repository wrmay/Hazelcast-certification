# modify the classpath according to your directory structure
# "../nfs/app/certification/lib/*" contains hazelcast-xxx.jar, hazelcast-client-xxx.jar, joda-time-2.7.jar and the certification project jar - build with your own implementation
# ../nfs/app/certification/resources contains FraudDetection.properties and hazelcast-client.xml
java -Xms3g -Xmx4g -XX:+UseParallelOldGC -XX:+UseParallelGC -XX:+UseCompressedOops -classpath "../nfs/app/certification/lib/*":../nfs/app/certification/resources com.hazelcast.certification.util.TransactionsGeneratorRunner
