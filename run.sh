#!/bin/bash

node_id=$1
if [ $node_id == 1 ] ; then
	rm infinispan.log
fi
mvn exec:exec -Dnodeid=$node_id
#mvn exec:java -Dexec.mainClass="se.elva.lkpg.locktest.LockTestRunner" -Dexec.systemProperties="jgroups.bind_addr=\"127.0.0.1\" java.net.preferIPv4Stack=true"
#mvn exec:exec -Dexec.executable="java" -Dexec.args="%classpath" -Dexec.mainClass="se.elva.lkpg.locktest.LockTestRunner"
