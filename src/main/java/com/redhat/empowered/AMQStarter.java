package com.redhat.empowered;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

public class AMQStarter {

	private BrokerService amqc01master;
	private BrokerService amqc02master;
	private BrokerService amqc01slave;
	private BrokerService amqc02slave;
	private BrokerService amqs01;
	
	

	public BrokerService getAmqc01master() {
		return amqc01master;
	}

	public void setAmqc01master(BrokerService amqc01master) {
		this.amqc01master = amqc01master;
	}

	public BrokerService getAmqc02master() {
		return amqc02master;
	}

	public void setAmqc02master(BrokerService amqc02master) {
		this.amqc02master = amqc02master;
	}

	public BrokerService getAmqc01slave() {
		return amqc01slave;
	}

	public void setAmqc01slave(BrokerService amqc01slave) {
		this.amqc01slave = amqc01slave;
	}

	public BrokerService getAmqc02slave() {
		return amqc02slave;
	}

	public void setAmqc02slave(BrokerService amqc02slave) {
		this.amqc02slave = amqc02slave;
	}

	public BrokerService getAmqs01() {
		return amqs01;
	}

	public void setAmqs01(BrokerService amqs01) {
		this.amqs01 = amqs01;
	}

	public void setup() throws URISyntaxException, Exception{
		amqc01master = BrokerFactory.createBroker(new URI("xbean:amq/amq-c01-master.xml"));
		amqc02master = BrokerFactory.createBroker(new URI("xbean:amq/amq-c02-master.xml"));
		amqc01slave = BrokerFactory.createBroker(new URI("xbean:amq/amq-c01-slave.xml"));
		amqc02slave = BrokerFactory.createBroker(new URI("xbean:amq/amq-c02-slave.xml"));
		amqs01 = BrokerFactory.createBroker(new URI("xbean:amq/amq-s01.xml"));
	}
	
	public void setupSingle() throws URISyntaxException, Exception{
		amqc02master = BrokerFactory.createBroker(new URI("xbean:amq/amq-c02-master.xml"));
		amqs01 = BrokerFactory.createBroker(new URI("xbean:amq/amq-s01.xml"));
	}
	
	public void noJmxSingle() {
		amqc02master.setUseJmx(false);
		amqs01.setUseJmx(false);
	}
	
	public void noJmx() {
		amqc01master.setUseJmx(false);
		amqc02master.setUseJmx(false);
		amqc01slave.setUseJmx(false);
		amqc02slave.setUseJmx(false);
		amqs01.setUseJmx(false);
	}
	 
	public void startMasters() throws Exception { 
		amqc01master.start();
		amqc02master.start();
		amqs01.start();
	}
	
	public void waitUntilMastersStarted() throws Exception { 
		amqc01master.waitUntilStarted();
		amqc02master.waitUntilStarted();
		amqs01.waitUntilStarted();
	}
	
	public void startSlaves() throws Exception { 
		amqc01slave.start();
		amqc02slave.start();
	}
	
	
	
	public void stopMasters() throws Exception { 
		amqc01master.stop();
		amqc02master.stop();
		amqs01.stop();
	}
	
	public void waitStopMasters() throws Exception { 
		amqc01master.waitUntilStopped();
		amqc02master.waitUntilStopped();
		amqs01.waitUntilStopped();
	}
	
	public void stopSlaves() throws Exception { 
		amqc01slave.stop();
		amqc02slave.stop();
	}
	
	public static void main(String[] args) throws Exception {
	    AMQStarter amqStarter = new AMQStarter();
	    amqStarter.setup();
	    amqStarter.startMasters();
	    Thread.currentThread().join();
	}
}
