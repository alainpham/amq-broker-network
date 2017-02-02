Project to show how network of brokers can work
===========================

You can find a detailed blog post explaining this example here :  

https://alainpham.github.io/posts/scalable-network-active-mq-brokers-handing-massive-connections/

run tests cases to see scenarios of scale up and scale down

  mvn test


to see interesteting logs only :

 mvn clean test>test.log & tail -f test.log | grep -E "TestAMQNetwork|TransportConnector|FailoverTransport|DiscoveryNetworkConnector"
