#!/bin/bash
scriptdir=`dirname $0`

envname=$1
shift

java -cp $scriptdir/../hazelcast-certification/target/hazelcast-certification-1.0-SNAPSHOT.jar  \
   -Dhazelcast.client.config=$scriptdir/$envname/hazelcast-client.xml \
   com.hazelcast.certification.util.Controller $*
