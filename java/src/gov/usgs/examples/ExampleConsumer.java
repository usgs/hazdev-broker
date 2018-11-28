package gov.usgs.examples;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import gov.usgs.hazdevbroker.Utility;
import gov.usgs.hazdevbroker.Consumer;

import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class ExampleConsumer {

	public static final String BROKER_CONFIG = "HazdevBrokerConfig";
	public static final String TOPIC_LIST = "TopicList";

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("Usage: ExampleConsumer <configfile> <optional logging configuration file>");
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

		// get topic list
		ArrayList<String> topicList = null;
		if (configJSON.containsKey(TOPIC_LIST)) {
			topicList = new ArrayList<String>();
			JSONArray topicArray = (JSONArray) configJSON.get(TOPIC_LIST);
			// convert to string collection
			for (int i = 0; i < topicArray.size(); i++) {

				// get the String
				String topic = (String) topicArray.get(i);
				topicList.add(topic);
			}
		} else {
			System.out
					.println("Error, did not find TopicList in configuration.");
			System.exit(1);
		}

		// nullcheck
		if (topicList == null) {
			System.out.println("Error, invalid TopicList from configuration.");
			System.exit(1);
		}

		// create consumer
		Consumer m_Consumer = new Consumer(brokerConfig);

		// subscribe to topics
		m_Consumer.subscribe(topicList);

		// run until stopped
		while (true) {

			try {
				// get message from broker
				ArrayList<String> brokerMessages = m_Consumer.pollString(100);

				// print messages
				for (int i = 0; i < brokerMessages.size(); i++) {
					System.out.println(brokerMessages.get(i));
				}
			} catch	(Exception e) {

				// log exception
				System.out.println(e.toString());
			}	
		}
	}
}
