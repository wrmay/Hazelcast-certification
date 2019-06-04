# Iteration 1

A very simplistic FraudDetection implementation was created (no Hazelcast yet)
and the transaction generator and server were run locally. It was observed that,
on occasion, the fraud detection server fail to read transactions even after a
connection was established and, at the same time, the transaction generator was successfully
writing to the channel.  A few changes were made to the fraud detection server
and transaction generator until they worked consistently in a local environment.

See [this commit](https://github.com/wrmay/Hazelcast-certification/commit/a58f72472ed5195cfc6e8f3ed4678c0087491d09).

# Iteration 2

Provisioning of an AWS environment was automated using [Cloudlab](https://pypi.org/project/cloudlab/)
and Ansible (see the "cloudlab" folder). The instance types selected were
m5.4x large (16 vCPUs, 64G RAM). A 5 minute run was performed.   


Observations from this run:  

- The provided fraud detection server prints throughput numbers every 3 seconds.  
The observed throughputs ranges from more than 200k/second to less than 10k/second.
Typical throughput was probably around 100k/second but its difficult to tell.
- it's hard to summarize the performance of a run concisely, add some metrics
to summarize the whole to make it easy to compare runs.  
- The wide range of through puts observed is almost certainly due to gc.  Need to
add a way to observe gc activity and also make a pass through the code looking for
simple ways to reduce the garbage produced (e.g. object pools).
