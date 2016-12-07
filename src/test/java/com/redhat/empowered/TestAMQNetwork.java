package com.redhat.empowered;


import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.FileSystemUtils;

public class TestAMQNetwork extends CamelSpringTestSupport {

	final static Logger logger = LoggerFactory.getLogger(TestAMQNetwork.class);

	private AMQStarter amqStarter;


	@Produce(uri = "s01-activemq:queue:sensor.events")
	protected ProducerTemplate inputEndpoint;

	@EndpointInject(uri = "mock:collector01")
	protected MockEndpoint collector01;
	@EndpointInject(uri = "mock:collector02")
	protected MockEndpoint collector02;
	@EndpointInject(uri = "mock:totalCollector")
	protected MockEndpoint totalCollector;

	protected ActiveMQQueue queue = new ActiveMQQueue("sensor.events");

	@Before
	public void setUp() throws Exception {
		super.setUp();

	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
		logger.info("Deleting data store folder ...");
		FileSystemUtils.deleteRecursively(new File("amq-c01-store"));
		FileSystemUtils.deleteRecursively(new File("amq-c02-store"));
		FileSystemUtils.deleteRecursively(new File("amq-s01-store"));
	}

	private void setupNominal() throws URISyntaxException, Exception, InterruptedException {
		amqStarter = new AMQStarter();
		amqStarter.setup();
		amqStarter.noJmx();
		amqStarter.startMasters();
		//wait for instances to fire up
		amqStarter.waitUntilMastersStarted();
		Thread.sleep(5000);
	}
	
	private void setupSingle() throws URISyntaxException, Exception, InterruptedException {
		amqStarter = new AMQStarter();
		amqStarter.setup();
		amqStarter.noJmx();
		amqStarter.getAmqc02master().start();
		amqStarter.getAmqs01().start();
		//wait for instances to fire up
		amqStarter.getAmqc02master().waitUntilStarted();
		amqStarter.getAmqs01().waitUntilStarted();

		Thread.sleep(3000);
	}
	
	@Test
	public void nominalSend1000Msg() throws Exception {

		setupNominal();
		
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				//create 2 consumers
				from("c01-activemq:queue:sensor.events").to(collector01).to(totalCollector);
				from("c02-activemq:queue:sensor.events").to(collector02).to(totalCollector);
			}
		});

		// Define some expectations
		collector01.setMinimumExpectedMessageCount(1);
		collector02.setMinimumExpectedMessageCount(1);
		totalCollector.setExpectedMessageCount(1000);
		// For now, let's just wait for some messages// TODO Add some expectations here
		logger.info("Start Sending 1000 messages");
		Thread.sleep(5000);
		logConnections(amqStarter.getAmqs01().getBroker());
		logConnections(amqStarter.getAmqc01master().getBroker());
		logConnections(amqStarter.getAmqc02master().getBroker());
		String message = "Hello World Message!";

		for (int i = 0; i < 1000; i++) {
			inputEndpoint.sendBody(message);
		}

		Thread.sleep(5000); //wait fore messages to drain

		logger.info("############### nominalSend1000Msg #########################");
		logStats(amqStarter);

		amqStarter.stopMasters();
		amqStarter.waitStopMasters();

		// Validate our expectations
		assertMockEndpointsSatisfied();


	}



	@Test
	public void scaleDown() throws Exception {

		setupNominal();
		
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				//create 2 consumers
				from("c01-activemq:queue:sensor.events").to(collector01).to(totalCollector);
				from("c02-activemq:queue:sensor.events").to(collector02).to(totalCollector);
			}
		});

		// Define some expectations
		collector01.setMinimumExpectedMessageCount(1);
		collector02.setMinimumExpectedMessageCount(1);
		totalCollector.setMinimumExpectedMessageCount(1000);
		// For now, let's just wait for some messages// TODO Add some expectations here
		logger.info("Start Sending 1000 messages");
		Thread.sleep(5000);
		logConnections(amqStarter.getAmqs01().getBroker());
		logConnections(amqStarter.getAmqc01master().getBroker());
		logConnections(amqStarter.getAmqc02master().getBroker());
		String message = "Hello World Me)ssage!";
		//send first 300 messages
		for (int i = 0; i < 500; i++) {
			inputEndpoint.sendBody(message);
		}
		Thread.sleep(1000);
		logger.info("############### before scaleDown #########################");
		logStats(amqStarter);
		logConnections(amqStarter.getAmqs01().getBroker());
		logConnections(amqStarter.getAmqc01master().getBroker());
		logConnections(amqStarter.getAmqc02master().getBroker());
		//stop the client connector : this can be done remotely through jolokia.
		amqStarter.getAmqc01master().getConnectorByName("clients").stop();
		for (int i = 0; i < 500; i++) {
			inputEndpoint.sendBody(message);
		}
		Thread.sleep(5000); //wait a bit for messages to drain
		logger.info("############### scaleDown #########################");
		logStats(amqStarter);
		logConnections(amqStarter.getAmqs01().getBroker());
		logConnections(amqStarter.getAmqc01master().getBroker());
		logConnections(amqStarter.getAmqc02master().getBroker());
		
		amqStarter.stopMasters();
		amqStarter.waitStopMasters();

		// Validate our expectations
		assertMockEndpointsSatisfied();


	}

	@Test
	public void onlyOneCentral() throws Exception {

		setupSingle();

		
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				//create 2 consumers
				from("c01-activemq:queue:sensor.events").to(collector01).to(totalCollector);
				from("c02-activemq:queue:sensor.events").to(collector02).to(totalCollector);
			}
		});

		// Define some expectations
		collector01.setMinimumExpectedMessageCount(1);
		collector02.setMinimumExpectedMessageCount(1);
		totalCollector.setExpectedMessageCount(1000);
		// For now, let's just wait for some messages// TODO Add some expectations here
		logger.info("Start Sending 1000 messages");
		Thread.sleep(5000);
		logConnections(amqStarter.getAmqs01().getBroker());
		logConnections(amqStarter.getAmqc02master().getBroker());
		String message = "Hello World Message!";
		for (int i = 0; i < 1000; i++) {
			inputEndpoint.sendBody(message);
		}

		Thread.sleep(5000); //wait a bit for messages to drain
		logger.info("############### onlyOneCentral #########################");
		logStats(amqStarter);
		logConnections(amqStarter.getAmqs01().getBroker());
		logConnections(amqStarter.getAmqc02master().getBroker());

		amqStarter.stopMasters();
		amqStarter.waitStopMasters();
		// Validate our expectations
		assertMockEndpointsSatisfied();
	}



	@Test
	public void scaleUp() throws Exception {

		setupSingle();

		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				//create 2 consumers
				from("c01-activemq:queue:sensor.events").to(collector01).to(totalCollector);
				from("c02-activemq:queue:sensor.events").to(collector02).to(totalCollector);
			}
		});

		// Define some expectations
		collector01.setMinimumExpectedMessageCount(1);
		collector02.setMinimumExpectedMessageCount(1);
		totalCollector.setExpectedMessageCount(1000);
		// For now, let's just wait for some messages// TODO Add some expectations here
		logger.info("Start Sending 1000 messages");
		Thread.sleep(5000);
		logConnections(amqStarter.getAmqs01().getBroker());
		logConnections(amqStarter.getAmqc02master().getBroker());
		String message = "Hello World Message!";
		//send first 500 messages
		for (int i = 0; i < 500; i++) {
			inputEndpoint.sendBody(message);
		}
		//start the broker and see that messages are redistributed
		amqStarter.setAmqc01master(BrokerFactory.createBroker(new URI("xbean:amq/amq-c01-master.xml")));
		amqStarter.getAmqc01master().setUseJmx(false);
		amqStarter.getAmqc01master().start();
		logger.info("############### waiting for instance to start #########################");
		amqStarter.getAmqc01master().waitUntilStarted(); //make sure instance has started
		logger.info("############### before scaleUp #########################");
		logStats(amqStarter);

		logger.info("############### waiting a bit longer to redistribute connections #########################");
		Thread.sleep(15000); //wait a bit for broker to connect and redistribute connections
		logConnections(amqStarter.getAmqs01().getBroker());
		logConnections(amqStarter.getAmqc01master().getBroker());
		logConnections(amqStarter.getAmqc02master().getBroker());
		logger.info("############### sending further messages #########################");
		
		for (int i = 0; i < 500; i++) {
			inputEndpoint.sendBody(message);
		}

		Thread.sleep(5000); //wait a bit for messages to drain
		logger.info("############### scaleUp #########################");
		logStats(amqStarter);

		amqStarter.stopMasters();
		amqStarter.waitStopMasters();
		// Validate our expectations
		assertMockEndpointsSatisfied();
	}

	private void logConnections(Broker broker) throws Exception {
		logger.info("Connected consumers on " + broker.getBrokerName() +  " : " + broker.getDestinationMap().get(queue).getConsumers().size());
		broker.getDestinationMap().get(queue).getConsumers().get(0).getConsumerInfo().getClientId();
		for (int i = 0; i < broker.getDestinationMap().get(queue).getConsumers().size(); i++) {
			String id = broker.getDestinationMap().get(queue).getConsumers().get(i).getConsumerInfo().getClientId();
			logger.info("Connected consumer : " + id);
		}
	}

	private void logStats(AMQStarter amqStarter) {
		logger.info("##############################################################");
		logger.info("##############################################################");
		logger.info("Received on collector01 " + collector01.getReceivedCounter());
		logger.info("Received on collector02 " + collector02.getReceivedCounter());
		logger.info("Total received " + totalCollector.getReceivedCounter());

		try {
			logger.info("amqs01 enqueue " + amqStarter.getAmqs01().getDestination(queue).getDestinationStatistics().getEnqueues().getCount());
			logger.info("amqs01 dequeue " + amqStarter.getAmqs01().getDestination(queue).getDestinationStatistics().getDequeues().getCount());
			logger.info("amqs01 dispatch " + amqStarter.getAmqs01().getDestination(queue).getDestinationStatistics().getDispatched().getCount());

		} catch (Exception e) {
		}
		
		try {
			logger.info("amqc01master enqueue " + amqStarter.getAmqc01master().getDestination(queue).getDestinationStatistics().getEnqueues().getCount());
			logger.info("amqc01master dequeue " + amqStarter.getAmqc01master().getDestination(queue).getDestinationStatistics().getDequeues().getCount());
			logger.info("amqc01master dispatch " + amqStarter.getAmqc01master().getDestination(queue).getDestinationStatistics().getDispatched().getCount());
		} catch (Exception e) {
		}

		try {
			logger.info("amqc02master enqueue " + amqStarter.getAmqc02master().getDestination(queue).getDestinationStatistics().getEnqueues().getCount());
			logger.info("amqc02master dequeue " + amqStarter.getAmqc02master().getDestination(queue).getDestinationStatistics().getDequeues().getCount());
			logger.info("amqc02master dispatch " + amqStarter.getAmqc02master().getDestination(queue).getDestinationStatistics().getDispatched().getCount());

		} catch (Exception e) {
		}


		logger.info("##############################################################");
		logger.info("##############################################################");
	}

	@Override
	protected ClassPathXmlApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext("META-INF/spring/camel-context.xml");
	}

}
