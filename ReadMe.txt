Project to show how network of brokers can work
===========================
run tests cases to see scenarios of scale up and scale down

  mvn test


to see interesteting logs only :

 mvn clean test>test.log &
 tail -f test.log | grep -E "TestAMQNetwork|TransportConnector|FailoverTransport|DiscoveryNetworkConnector"
