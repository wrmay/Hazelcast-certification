# modify the classpath according to your directory structure
# "../nfs/app/certification/lib/*" contains hazelcast-xxx.jar, hazelcast-client-xxx.jar, joda-time-2.7.jar and the certification project jar - build with your own implementation
# ../nfs/app/certification/resources contains FraudDetection.properties and hazelcast-client.xml
java -Xms8g -Xmx8g -Xmn4g -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+UseCompressedOops -classpath target/hazelcast-certification-1.0-SNAPSHOT.jar com.hazelcast.certification.util.TransactionsGeneratorRunner
