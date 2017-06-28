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

/**
 * a client class used to consume messages out of one or more hazdev-broker
 * (kafka) topics and write the messages to files based on the provided
 * configuration
 *
 * @author U.S. Geological Survey &lt;jpatton at usgs.gov&gt;
 */
public class ConsumerClient {

	/**
	 * JSON Configuration Keys
	 */
	public static final String LOG4J_CONFIGFILE = "Log4JConfigFile";
	public static final String BROKER_CONFIG = "HazdevBrokerConfig";
	public static final String TOPIC_LIST = "TopicList";
	public static final String FILE_EXTENSION = "FileExtension";
	public static final String FILE_NAME = "FileName";
	public static final String MESSAGES_PER_FILE = "MessagesPerFile";
	public static final String TIME_PER_FILE = "TimePerFile";
	public static final String OUTPUT_DIRECTORY = "OutputDirectory";

	/**
	 * Required configuration string defining the output directory
	 */
	private static String outputDirectory;

	/**
	 * Required configuration string defining the output file extension
	 */
	private static String fileExtension;

	/**
	 * Optional configuration string defining the output file name
	 */
	private static String fileName;

	/**
	 * Optional configuration Long defining the number of messages per file,
	 * default is one.
	 */
	private static Long messagesPerFile;

	/**
	 * Optional configuration Long defining the number seconds before writing a
	 * file with less than the configured number of messages, default is null
	 */
	private static Long timePerFile;

	/**
	 * Log4J logger for ConsumerClient
	 */
	static Logger logger = Logger.getLogger(ConsumerClient.class);

	/**
	 * Queue object to hold messages that need to be written to the file
	 */
	private static Queue<String> fileQueue;

	/**
	 * Variable containing time the last file was written.
	 */
	private static Long lastFileWriteTime;

	/**
	 * main function for ConsumerClient
	 *
	 * @param args
	 *            - A String[] containing the command line arguments.
	 */
	public static void main(String[] args) {

		// check number of arguments
		if (args.length == 0) {
			System.out.println("Usage: hazdev-broker <configfile>");
			System.exit(1);
		}

		// init to default values
		fileQueue = new LinkedList<String>();
		outputDirectory = null;
		fileExtension = null;
		fileName = new String();
		messagesPerFile = (long) 1;
		timePerFile = null;

		// init last write time to now
		lastFileWriteTime = (Long) (System.currentTimeMillis() / 1000);

		// get config file name
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
			logger.info("Using configured fileExtension of: " + fileExtension);
		} else {
			logger.error("Error, did not find FileExtension in configuration.");
			System.exit(1);
		}

		// get file name
		if (configJSON.containsKey(FILE_NAME)) {
			fileName = (String) configJSON.get(FILE_NAME);
			logger.info("Using configured fileName of: " + fileName);
		} else {
			fileName = "";
			logger.info("Not using configured fileName.");
		}

		// get output directory
		if (configJSON.containsKey(OUTPUT_DIRECTORY)) {
			outputDirectory = (String) configJSON.get(OUTPUT_DIRECTORY);
			logger.info(
					"Using configured outputDirectory of: " + outputDirectory);
		} else {
			logger.error(
					"Error, did not find OutputDirectory in configuration.");
			System.exit(1);
		}

		// get messages per file
		if (configJSON.containsKey(MESSAGES_PER_FILE)) {
			messagesPerFile = (Long) configJSON.get(MESSAGES_PER_FILE);
			logger.info("Using configured messagesPerFile of: "
					+ messagesPerFile.toString());
		} else {
			messagesPerFile = (long) 1;
			logger.info("Using default messagesPerFile of: "
					+ messagesPerFile.toString());
		}

		// get time per file
		if (configJSON.containsKey(TIME_PER_FILE)) {
			timePerFile = (Long) configJSON.get(TIME_PER_FILE);
			logger.info("Using configured timePerFile of: "
					+ timePerFile.toString());
		} else {
			logger.info("Not using timePerFile.");
		}

		// get broker config
		JSONObject brokerConfig = null;
		if (configJSON.containsKey(BROKER_CONFIG)) {
			brokerConfig = (JSONObject) configJSON.get(BROKER_CONFIG);
		} else {
			logger.error(
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
			logger.error("Error, did not find TopicList in configuration.");
			System.exit(1);
		}

		// nullcheck
		if (topicList == null) {
			logger.error("Error, invalid TopicList from configuration.");
			System.exit(1);
		}

		logger.info("Processed Config.");

		// create consumer
		Consumer m_Consumer = new Consumer(brokerConfig);

		// subscribe to topics
		m_Consumer.subscribe(topicList);

		logger.info("Created Consumer.");

		// run until stopped
		while (true) {

			// get messages from broker
			ArrayList<String> brokerMessages = m_Consumer.pollString(500);

			// nullcheck brokerMessages
			if (brokerMessages == null) {
				continue;
			}

			// add all messages in brokerMessages to queue
			for (int i = 0; i < brokerMessages.size(); i++) {

				// get string
				String message = brokerMessages.get(i);
				logger.debug(message);

				// add string
				fileQueue.add(message);
			}

			// check to see if we have anything to write
			if (fileQueue.isEmpty()) {

				// nothign to do
				logger.debug("No messages to write.");
				continue;
				// check to see if we have enough messages to write
			} else if (fileQueue.size() >= messagesPerFile) {

				// we've got enough messages
				logger.info("Writing output file due to number of messages, "
						+ String.valueOf(fileQueue.size()) + " pending. ");

				// write messagesPerFile worth of messages
				writeMessagesToDisk(messagesPerFile.intValue());
				// otherwise check to see if it's been long enough to force
				// a file
			} else if (timePerFile != null) {

				// get current time in seconds
				Long timeNow = System.currentTimeMillis() / 1000;

				// calculate elapsed time
				Long elapsedTime = timeNow - lastFileWriteTime;

				// has it been long enough:
				if (elapsedTime > timePerFile) {
					logger.info("Writing output file due to time, "
							+ elapsedTime.toString()
							+ " seconds since last file");

					// write all pending messages in the queue to disk
					// we're sure there are less than messagesPerFile
					// because otherwise that would have been handled above
					writeMessagesToDisk(fileQueue.size());
				}

			}
		}
	}

	/**
	 * File writing function for ConsumerClient
	 *
	 * @param numToWrite
	 *            - An Integer containing the number of messages to write in
	 *            this file.
	 * @return Returns true if successful, false otherwise
	 */
	public static boolean writeMessagesToDisk(Integer numToWrite) {

		try {
			// get current time in milliseconds
			Long timeNow = System.currentTimeMillis();

			// build filename from desired output directory, time, optional
			// name,
			// and extension
			String outFileName = outputDirectory + "/" + timeNow.toString()
					+ fileName + "." + fileExtension;

			// create an UTF-8 formatted printwriter to write to disk
			PrintWriter fileWriter = new PrintWriter(outFileName, "UTF-8");

			for (int i = 0; i < numToWrite; i++) {

				// don't try to write if we're out of messages
				if (fileQueue.isEmpty()) {
					continue;
				}

				// get the next message to write
				String messageString = fileQueue.remove();

				// check to see if we were newline terminated, add a newline
				// if we were not
				if (messageString.charAt(messageString.length() - 1) != '\n') {
					messageString = messageString.concat("\n");
				}

				// just call print
				fileWriter.print(messageString);
			}

			// done with file
			fileWriter.close();

			// Remember the time we wrote this file in seconds
			lastFileWriteTime = timeNow / 1000;

		} catch (Exception e) {

			// log exception
			logger.error(e.toString());
			return (false);
		}

		return (true);
	}

}
