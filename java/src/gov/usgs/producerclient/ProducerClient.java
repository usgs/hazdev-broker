package gov.usgs.producerclient;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import gov.usgs.hazdevbroker.Utility;
import gov.usgs.hazdevbroker.Producer;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * a client class used to produce messages to a hazdev-broker (kafka) topic and
 * from messages in files based on the provided configuration
 *
 * @author U.S. Geological Survey &lt;jpatton at usgs.gov&gt;
 */
public class ProducerClient {
	/**
	 * JSON Configuration Keys
	 */
	public static final String TYPE_KEY = "Type";
	public static final String LOG4J_CONFIGFILE = "Log4JConfigFile";
	public static final String BROKER_CONFIG = "HazdevBrokerConfig";
	public static final String TOPIC = "Topic";
	public static final String FILE_EXTENSION = "FileExtension";
	public static final String TIME_PER_FILE = "TimePerFile";
	public static final String INPUT_DIRECTORY = "InputDirectory";
	public static final String ARCHIVE_DIRECTORY = "ArchiveDirectory";
	public static final String HEARTBEAT_INTERVAL = "HeartbeatInterval";

	/**
	 * Required configuration string defining the input directory
	 */
	private static String inputDirectory;

	/**
	 * Optional configuration string defining the archive directory
	 */
	private static String archiveDirectory;

	/**
	 * Required configuration string defining the input file extension
	 */
	private static String fileExtension;

	/**
	 * Optional configuration Long defining the number seconds before reading a
	 * file, default is null
	 */
	private static Long timePerFile;

	/**
	 * Optional configuration Long defining the number seconds between sending
	 * heartbeat messages, default is null
	 */
	private static Long heartbeatInterval;

	/**
	 * Variable containing time the last heartbeat was sent.
	 */
	private static Long lastHeartbeatTime;

	/**
	 * Log4J logger for ProducerClient
	 */
	static Logger logger = Logger.getLogger(ProducerClient.class);

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
					"Usage: hazdev-broker ProducerClient <configfile>");
			System.exit(1);
		}

		// init to default values
		inputDirectory = null;
		archiveDirectory = null;
		fileExtension = null;
		timePerFile = null;
		heartbeatInterval = null;

		// init last heartbeat time to now
		lastHeartbeatTime = (Long) (System.currentTimeMillis() / 1000);

		// parse config file into json
		JSONObject configJSON = Utility.readConfigurationFromFile(args[0]);

		// nullcheck
		if (configJSON == null) {
			System.out.println("Error, invalid json from configuration.");
			System.exit(1);
		}

		// type check
		if (configJSON.containsKey(TYPE_KEY)) {
			String type = configJSON.get(TYPE_KEY).toString();
			if (!type.equals("ProducerClient")) {
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

		// get input directory
		if (configJSON.containsKey(INPUT_DIRECTORY)) {
			inputDirectory = (String) configJSON.get(INPUT_DIRECTORY);
			logger.info(
					"Using configured inputDirectory of: " + inputDirectory);
		} else {
			logger.error(
					"Error, did not find InputDirectory in configuration.");
			System.exit(1);
		}

		// get archive directory
		if (configJSON.containsKey(ARCHIVE_DIRECTORY)) {
			archiveDirectory = (String) configJSON.get(ARCHIVE_DIRECTORY);
			logger.info("Using configured archiveDirectory of: "
					+ archiveDirectory);

			// create archive directory if it doesn't exist
			File archDir = new File(archiveDirectory);
			if (!archDir.exists()) {
				archDir.mkdirs();
			}
		} else {
			logger.info("Not using archiveDirectory.");
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
			logger.info("Not using heartbeatInterval, not sending heartbeat "
						+ "messages.");
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

		// get topic
		String topic = null;
		if (configJSON.containsKey(TOPIC)) {
			topic = (String) configJSON.get(TOPIC);

			logger.info("Using configured Topic of: " + topic);
		} else {
			logger.error("Error, did not find Topic in configuration.");
			System.exit(1);
		}

		logger.info("Processed Config.");

		// create producer
		Producer m_Producer = new Producer(brokerConfig, heartbeatInterval);

		logger.info("Startup, version : " + 
			m_Producer.VERSION_MAJOR + "." + m_Producer.VERSION_MINOR + "." + 
			m_Producer.VERSION_PATCH);

		// run until stopped
		while (true) {

			ArrayList<String> messageList = readMessagesFromFile();

			// if we have anything to send
			if (messageList != null) {

				// send each message
				for (int i = 0; i < messageList.size(); i++) {

					// get message
					String message = messageList.get(i);

					// log it
					logger.debug("Sending message: " + message);

					// send message
					m_Producer.sendString(topic, message);
				}
			} else {

				logger.debug("Sending idle heartbeat");
				
				// send a heartbeat
				m_Producer.sendHeartbeat(topic);
			}

			// wait a bit before the next file
			if (timePerFile != null) {

				try {
					Thread.sleep(timePerFile * 1000);
				} catch (InterruptedException ex) {
					logger.error(ex.toString());
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	/**
	 * File reading function for ProducerClient
	 *
	 * @return Returns an ArrayList of messages as Strings, null otherwise
	 */
	public static ArrayList<String> readMessagesFromFile() {

		ArrayList<String> messageList = null;
		try {
			// set up to search the input directory
			File dir = new File(inputDirectory);

			// list all the files in the directory
			for (File inputFile : dir.listFiles()) {
				// if the file has the right extension
				if (inputFile.getName().endsWith((fileExtension))) {

					logger.debug("Found File: " + inputFile.getName());

					// create message list
					messageList = new ArrayList<String>();

					// read the file
					BufferedReader inputReader = null;

					try {
						inputReader = new BufferedReader(
								new FileReader(inputFile));
						String text = null;

						// each line is assumed to be a message
						while ((text = inputReader.readLine()) != null) {
							messageList.add(text);
						}
					} catch (FileNotFoundException e) {
						logger.error("readMessagesFromFile: " + e.toString());
					} catch (IOException e) {
						logger.error("readMessagesFromFile: " + e.toString());
					} finally {
						try {
							if (inputReader != null) {
								inputReader.close();
							}
						} catch (IOException e) {
							logger.error("readMessagesFromFile: " + 
								e.toString());
						}
					}

					// done with the file
					if (archiveDirectory == null) {

						// not archiving, just delete it
						inputFile.delete();
					} else {
						// Move file to archive directory
						inputFile.renameTo(new File(
								archiveDirectory + File.separatorChar + 
								inputFile.getName()));
					}

					// only handle one file at a time
					break;
				}
			}
		} catch (Exception e) {

			// log exception
			logger.error("readMessagesFromFile: " + e.toString());
			return (null);
		}

		return (messageList);
	}
}
