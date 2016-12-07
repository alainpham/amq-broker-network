//package com.redhat.empowered;
//
//
//import java.net.URI;
//
//import org.apache.activemq.command.ActiveMQQueue;
//import org.apache.camel.EndpointInject;
//import org.apache.camel.Produce;
//import org.apache.camel.ProducerTemplate;
//import org.apache.camel.builder.RouteBuilder;
//import org.apache.camel.component.mock.MockEndpoint;
//import org.apache.camel.test.spring.CamelSpringTestSupport;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//
//public class TestSending extends CamelSpringTestSupport {
//
//	final static Logger logger = LoggerFactory.getLogger(TestSending.class);
//
//
//	@Produce(uri = "s01-activemq:queue:sensor.events")
//	protected ProducerTemplate inputEndpoint;
//
//	@EndpointInject(uri = "mock:collector01")
//	protected MockEndpoint collector01;
//	@EndpointInject(uri = "mock:collector02")
//	protected MockEndpoint collector02;
//	@EndpointInject(uri = "mock:totalCollector")
//	protected MockEndpoint totalCollector;
//
//	protected ActiveMQQueue queue = new ActiveMQQueue("sensor.events");
//
//	@Before
//	public void setUp() throws Exception {
//		super.setUp();
//
//	}
//
//	@After
//	public void tearDown() throws Exception {
//		super.tearDown();
//	}
//
//	@Test
//	public void scaleUp() throws Exception {
//
//		context.addRoutes(new RouteBuilder() {
//			@Override
//			public void configure() throws Exception {
//				//create 2 consumers
////				from("c01-activemq:queue:sensor.events").to(collector01).to(totalCollector);
////				from("c02-activemq:queue:sensor.events").to(collector02).to(totalCollector);
//			}
//		});
//
//		// Define some expectations
////		collector01.setMinimumExpectedMessageCount(1);
////		collector02.setMinimumExpectedMessageCount(1);
////		totalCollector.setExpectedMessageCount(1000);
//		// For now, let's just wait for some messages// TODO Add some expectations here
//		logger.info("Start Sending 1000 messages");
//		String message = "Hello World Message!";
//		//send first 500 messages
//		for (int i = 0; i < 500; i++) {
//			inputEndpoint.sendBody(message);
//		}
//		//start the broker and see that messages are redistributed
//		logger.info("############### Start instance now!!! #########################");
//		Thread.sleep(30000); //wait a bit for broker to connect and redistribute connections
//		logger.info("############### sending further messages #########################");
//		
//		for (int i = 0; i < 500; i++) {
//			inputEndpoint.sendBody(message);
//		}
//
//		Thread.sleep(5000); //wait a bit for messages to drain
//		logger.info("############### scaleUp #########################");
//
//		// Validate our expectations
//		assertMockEndpointsSatisfied();
//	}
//
//
//
//	@Override
//	protected ClassPathXmlApplicationContext createApplicationContext() {
//		return new ClassPathXmlApplicationContext("META-INF/spring/camel-context.xml");
//	}
//
//}
