package com.redhat.empowered;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

public class AMQS01 {

	public static void main(String[] args) throws Exception {
		BrokerFactory.createBroker(new URI("xbean:amq/amq-s01.xml")).start();
	    Thread.currentThread().join();
	}
}
