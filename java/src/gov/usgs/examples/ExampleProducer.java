package gov.usgs.examples;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import gov.usgs.hazdevbroker.Utility;
import gov.usgs.hazdevbroker.Producer;

import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class ExampleProducer {

	public static final String BROKER_CONFIG = "HazdevBrokerConfig";
	public static final String TOPIC = "Topic";

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("Usage: ExampleProducer <configfile>  <optional logging configuration file>");
			System.exit(1);
		}

		// init log4j
		if (args.length == 1) {
			System.out.println("Using default logging configuration");
			BasicConfigurator.configure();
		} else if (args.length >= 2) {
			System.out.println("Using custom logging configuration");
			PropertyConfigurator.configure(args[1]);
		}

		// parse config file into json
		JSONObject configJSON = Utility.readConfigurationFromFile(args[0]);

		// nullcheck
		if (configJSON == null) {
			System.out.println("Error, invalid json from configuration.");
			System.exit(1);
		}

		// get broker config
		JSONObject brokerConfig = null;
		if (configJSON.containsKey(BROKER_CONFIG)) {
			brokerConfig = (JSONObject) configJSON.get(BROKER_CONFIG);
		} else {
			System.out.println(
					"Error, did not find HazdevBrokerConfig in configuration.");
			System.exit(1);
		}

		// get topic
		String topic = "";
		if (configJSON.containsKey(TOPIC)) {
			topic = (String) configJSON.get(TOPIC);
		} else {
			System.out.println("Error, did not find Topic in configuration.");
			System.exit(1);
		}

		// create producer
		Producer m_Producer = new Producer(brokerConfig, null);

		// run until stopped
		while (true) {

			// get message to send
			String message = System.console().readLine();

			// send message
			m_Producer.sendString(topic, message);
		}
	}
}
