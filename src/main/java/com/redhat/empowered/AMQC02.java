package com.redhat.empowered;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

public class AMQC02 {

	public static void main(String[] args) throws Exception {
		BrokerFactory.createBroker(new URI("xbean:amq/amq-c02-master.xml")).start();
	    Thread.currentThread().join();
	}
}
