package gov.usgs.consumerclient;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import gov.usgs.hazdevbroker.Utility;
import gov.usgs.hazdevbroker.Consumer;
import gov.usgs.hazdevbroker.Heartbeat;

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
	public static final String TYPE_KEY = "Type";
	public static final String LOG4J_CONFIGFILE = "Log4JConfigFile";
	public static final String BROKER_CONFIG = "HazdevBrokerConfig";
	public static final String TOPIC_LIST = "TopicList";
	public static final String FILE_EXTENSION = "FileExtension";
	public static final String FILE_NAME = "FileName";
	public static final String MESSAGES_PER_FILE = "MessagesPerFile";
	public static final String TIME_PER_FILE = "TimePerFile";
	public static final String OUTPUT_DIRECTORY = "OutputDirectory";
	public static final String HEARTBEAT_INTERVAL = "HeartbeatInterval";
	public static final String WRITE_HEARTBEAT_FILE = "WriteHeartbeatFile";
	private static final String COMMENT_IDENTIFIER = "#";

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
	 * Optional configuration Long defining the number seconds between expecting
	 * heartbeat messages, default is null
	 */
	private static Long heartbeatInterval;

	/**
	 * Optional configuration boolean defining whether to write heartbeat files,
	 * default is null
	 */
	private static Boolean writeHeartbeatFile;

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
			System.out.println(
					"Usage: hazdev-broker ConsumerClient <configfile>");
			System.exit(1);
		}

		// init to default values
		fileQueue = new LinkedList<String>();
		outputDirectory = null;
		fileExtension = null;
		fileName = new String();
		messagesPerFile = (long) 1;
		timePerFile = null;
		heartbeatInterval = null;
		writeHeartbeatFile = (boolean) false;

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
			String line = null;

			while ((line = configReader.readLine()) != null) {
				// strip any comments
				String strippedLine = Utility.stripCommentsFromLine(line, 
					COMMENT_IDENTIFIER);
				if ((strippedLine != null) && (!line.isEmpty())) {
					configBuffer.append(strippedLine).append("\n");
				}
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
				e.printStackTrace();
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

		// type check
		if (configJSON.containsKey(TYPE_KEY)) {
			String type = configJSON.get(TYPE_KEY).toString();
			if (!type.equals("ConsumerClient")) {
				System.out.println("Error, wrong configuration.");
				System.exit(1);
			}
		} else {
			System.out.println("Error, missing type in configuration.");
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

			// create output directory if it doesn't exist
			File outDir = new File(outputDirectory);
			if (!outDir.exists()) {
				outDir.mkdirs();
			}
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

		// get hearbeat interval
		if (configJSON.containsKey(HEARTBEAT_INTERVAL)) {
			heartbeatInterval = (Long) configJSON.get(HEARTBEAT_INTERVAL);
			logger.info("Using configured heartbeatInterval of: "
					+ heartbeatInterval.toString());
		} else {
			logger.info("Not using heartbeatInterval, not expecting heartbeat "
						+ "messages.");
		}

		// get write heartbeat file
		if (configJSON.containsKey(WRITE_HEARTBEAT_FILE)) {
			writeHeartbeatFile = (Boolean) configJSON.get(WRITE_HEARTBEAT_FILE);
			logger.info("Using configured writeHeartbeatFile of: "
					+ writeHeartbeatFile.toString());
		} else {
			logger.info("Not using writeHeartbeatFile, not writing heartbeat "
						+ "files.");
		}

		// setup heartbeat files
		String heartbeatDirectory = null;
		if (writeHeartbeatFile == true) {
			heartbeatDirectory = outputDirectory;
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
		Consumer m_Consumer = new Consumer(brokerConfig, heartbeatDirectory);

		// subscribe to topics
		m_Consumer.subscribe(topicList);

		logger.info("Startup, version : " + 
			m_Consumer.VERSION_MAJOR + "." + m_Consumer.VERSION_MINOR + "." + 
			m_Consumer.VERSION_PATCH);

		// run until stopped
		while (true) {

			// if we are checking heartbeat times
			if (heartbeatInterval != null) {

				// get current time in seconds
				Long timeNow = System.currentTimeMillis() / 1000;

				// get last heartbeat time
				Long lastHB = m_Consumer.getLastHeartbeatTime();

				// calculate elapsed time
				Long elapsedTime = timeNow - lastHB;

				// has it been too long since the last heartbeat?
				if (elapsedTime > heartbeatInterval) {
					logger.error("No Heartbeat Message seen from topic(s)" + 
						" in " + heartbeatInterval.toString() + " seconds! (" +
						elapsedTime.toString() + ")");

					// reset last heartbeat time so that we don't fill the 
					// log
					m_Consumer.setLastHeartbeatTime(timeNow);
				} else {
					logger.debug("Heartbeat seen from topic(s) (" +
						elapsedTime.toString() + ")");
				}
			}

			// get any messages from broker
			try {
				ArrayList<String> brokerMessages = m_Consumer.pollString(500);

				// nullcheck brokerMessages (null means no messages)
				if (brokerMessages == null) {
					continue;
				}

				// add all messages in brokerMessages to file queue
				for (int i = 0; i < brokerMessages.size(); i++) {

					// get message as string
					String message = brokerMessages.get(i);
					logger.debug(message);

					// add string to queue
					fileQueue.add(message);
				}

				// write file containing messages to disk
				// check to see if we have anything to write
				if (fileQueue.isEmpty()) {

					// nothing to do
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
			} catch	(Exception e) {

				// log exception
				logger.error(e.toString());
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
			logger.error("writeMessagesToDisk: " + e.toString());
			return (false);
		}

		return (true);
	}

}
