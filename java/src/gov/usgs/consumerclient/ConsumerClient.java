package gov.usgs.consumerclient;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import gov.usgs.hazdevbroker.Consumer;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ConsumerClient {

	public static final String LOG4J_CONFIGFILE = "Log4JConfigFile";
	public static final String BROKER_CONFIG = "HazdevBrokerConfig";
	public static final String TOPIC_LIST = "TopicList";
	public static final String FILE_EXTENSION = "FileExtension";
	public static final String MESSAGES_PER_FILE = "MessagesPerFile";
	public static final String TIME_PER_FILE = "TimePerFile";
	public static final String OUTPUT_DIRECTORY = "OutputDirectory";

	// queue to hold messages to be written to file
	private static Queue<String> fileQueue;

	// configuration values
	private static String outputDirectory;
	private static String fileExtension;
	private static Long messagesPerFile;
	private static Long timePerFile;

	private static Long lastFileWriteTime;

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("Usage: ConsumerClient <configfile>");
			System.exit(1);
		}

		fileQueue = new LinkedList<String>();
		outputDirectory = null;
		fileExtension = null;
		messagesPerFile = (long) 1;
		timePerFile = null;

		// init last write time to now
		lastFileWriteTime = (Long) (System.currentTimeMillis() / 1000);

		String configFileName = args[0];

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

		System.out.println("Read Config.");

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

		System.out.println("Parsed Config.");

		// get log4j config
		String logConfigString = null;
		if (configJSON.containsKey(LOG4J_CONFIGFILE)) {
			logConfigString = (String) configJSON.get(LOG4J_CONFIGFILE);
			System.out.println("Using custom logging configuration");
			PropertyConfigurator.configure(logConfigString);
		} else {
			System.out.println("Using default logging configuration");
			BasicConfigurator.configure();
		}

		// get file extension
		if (configJSON.containsKey(FILE_EXTENSION)) {
			fileExtension = (String) configJSON.get(FILE_EXTENSION);
		} else {
			System.out.println(
					"Error, did not find FileExtension in configuration.");
			System.exit(1);
		}

		System.out.println("Got Extension.");

		// get output directory
		if (configJSON.containsKey(OUTPUT_DIRECTORY)) {
			outputDirectory = (String) configJSON.get(OUTPUT_DIRECTORY);
		} else {
			System.out.println(
					"Error, did not find OutputDirectory in configuration.");
			System.exit(1);
		}

		System.out.println("Got output directory.");


		// get messages per file
		if (configJSON.containsKey(MESSAGES_PER_FILE)) {
			messagesPerFile = (Long)configJSON.get(MESSAGES_PER_FILE);
		} else {
			messagesPerFile = (long) 1;
		}

		System.out.println("Got Messages per file.");

		// get time per file
		if (configJSON.containsKey(TIME_PER_FILE)) {
			timePerFile = (Long) configJSON.get(TIME_PER_FILE);
		}

		System.out.println("Got Time per file.");

		// get broker config
		JSONObject brokerConfig = null;
		if (configJSON.containsKey(BROKER_CONFIG)) {
			brokerConfig = (JSONObject) configJSON.get(BROKER_CONFIG);
		} else {
			System.out.println(
					"Error, did not find HazdevBrokerConfig in configuration.");
			System.exit(1);
		}

		System.out.println("Got broker config.");

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

		System.out.println("Got topic list.");

		System.out.println("Processed Config.");

		// create consumer
		Consumer m_Consumer = new Consumer(brokerConfig);

		// subscribe to topics
		m_Consumer.subscribe(topicList);

		System.out.println("Created Consumer.");

		// run until stopped
		while (true) {

			// get messages from broker
			ArrayList<String> brokerMessages = m_Consumer.pollString(500);

			// did we get anything?
			if (brokerMessages == null) {
				continue;
			}

			// add messages to queue
			for (int i = 0; i < brokerMessages.size(); i++) {
				String message = brokerMessages.get(i);
				System.out.println(message);
				fileQueue.add(message);
			}

			// check to see if we have anything to write
			if (fileQueue.isEmpty()) {
				System.out.println("Nothing to write.");
				continue;
			}

			// check to see if we have enough messages to write
			if (fileQueue.size() >= messagesPerFile) {
				System.out.println("Writing message due to number of messages.");
				System.out.println(String.valueOf(fileQueue.size()));
				writeMessagesToDisk(messagesPerFile.intValue());
			// otherwise check to see if it's been long enough to force
			// a file
			} else if (timePerFile != null) {

				// get current time in seconds
				Long timeNow = System.currentTimeMillis() / 1000;

				// has it been long enough:
				if ((timeNow - lastFileWriteTime) > timePerFile) {
					System.out.println("Writing message due to time.");
					// write all pending messages to disk
					writeMessagesToDisk(fileQueue.size());
				}

			}
		}
	}

	public static boolean writeMessagesToDisk(Integer numToWrite) {

		try {
			// get current time in milliseconds
			Long timeNow = System.currentTimeMillis();

			// build filename from desired output directory, time, and extension
			String fileName = outputDirectory + "/" + timeNow.toString() + "."
					+ fileExtension;

			PrintWriter fileWriter = new PrintWriter(fileName, "UTF-8");

			for (int i = 0; i < numToWrite; i++) {
				// don't try to write if we're out of messages
				if (fileQueue.isEmpty()) {
					continue;
				}

				String messageString = fileQueue.remove();
				//fileWriter.println(messageString);
				fileWriter.print(messageString);
			}

			fileWriter.close();

			// Remember the time we wrote this file in seconds
			lastFileWriteTime = timeNow / 1000;

		} catch (Exception e) {

			System.out.println(e.toString());
			return (false);
		}

		return (true);
	}

}
