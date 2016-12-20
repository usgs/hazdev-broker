package gov.usgs.examples;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import gov.usgs.hazdevbroker.Producer;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ExampleProducer {

	public static final String BROKER_CONFIG = "HazdevBrokerConfig";
	public static final String TOPIC = "Topic";

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("Usage: ExampleProducer <configfile>  <optional logging configuration file>");
			System.exit(1);
		}

		String configFileName = args[0];

		// init log4j
		if (args.length == 1) {
			System.out.println("Using default logging configuration");
			BasicConfigurator.configure();
		} else if (args.length >= 2) {
			System.out.println("Using custom logging configuration");
			PropertyConfigurator.configure(args[1]);
		}

		// read the config file
		File configFile = new File(configFileName);
		BufferedReader configReader = null;
		StringBuffer configBuffer = new StringBuffer();

		try {
			configReader = new BufferedReader(new FileReader(configFile));
			String text = null;

			while ((text = configReader.readLine()) != null) {
				configBuffer.append(text).append("\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (configReader != null) {
					configReader.close();
				}
			} catch (IOException e) {
			}
		}

		// parse config file into json
		JSONObject configJSON = null;
		try {
			JSONParser configParser = new JSONParser();
			configJSON = (JSONObject) configParser
					.parse(configBuffer.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}

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
		Producer m_Producer = new Producer(brokerConfig);

		// run until stopped
		while (true) {

			// get message to send
			String message = System.console().readLine();

			// send message
			m_Producer.sendString(topic, message);
		}
	}
}
