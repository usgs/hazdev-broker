package gov.usgs.archiveclient;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import gov.usgs.hazdevbroker.Utility;
import gov.usgs.hazdevbroker.Consumer;

import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

/**
 * a client class used to archive messages out of one or more hazdev-broker
 * (kafka) topics and write the messages to daily files based on the provided
 * configuration
 *
 * @author U.S. Geological Survey &lt;jpatton at usgs.gov&gt;
 */
public class ArchiveClient {

	/**
	 * JSON Configuration Keys
	 */
	public static final String TYPE_KEY = "Type";
	public static final String LOG4J_CONFIGFILE = "Log4JConfigFile";
	public static final String BROKER_CONFIG = "HazdevBrokerConfig";
	public static final String TOPIC_LIST = "TopicList";
	public static final String FILE_EXTENSION = "FileExtension";
	public static final String FILE_NAME = "FileName";
	public static final String OUTPUT_DIRECTORY = "OutputDirectory";
	public static final String POLL_TIMEOUT = "PollTimeout";

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
	 * Optional configuration value defining the maximum time to wait before 
	 * returning from polling the consumer in seconds
	 */
	private static long pollTimeout = 10;

	/**
	 * Log4J logger for ConsumerClient
	 */
	static Logger logger = Logger.getLogger(ArchiveClient.class);

	/**
	 * Optional configuration string defining the output file name
	 */
	private static String outFileName;

	/**
	 * main function for ArchiveClient
	 *
	 * @param args
	 *            - A String[] containing the command line arguments.
	 */
	public static void main(String[] args) {

		// check number of arguments
		if (args.length == 0) {
			System.out
					.println("Usage: hazdev-broker ArchiveClient <configfile>");
			System.exit(1);
		}

		// init to default values
		outputDirectory = null;
		fileExtension = null;
		fileName = new String();

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
			if (!type.equals("ArchiveClient")) {
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

		logger.info("----------Archive Client Startup----------");

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

		// get file name
		if (configJSON.containsKey(POLL_TIMEOUT)) {
			pollTimeout = (long) configJSON.get(POLL_TIMEOUT);
			logger.info("Using configured poll timeout of: " + String.valueOf(pollTimeout));
		} else {
			logger.info("Using default poll timeout of: " + String.valueOf(pollTimeout));
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

		logger.info("----------Processed Config----------");

		// create consumer
		Consumer m_Consumer = new Consumer(brokerConfig);

		// subscribe to topics
		m_Consumer.subscribe(topicList);

		logger.info("Broker version: " + 
			m_Consumer.VERSION_MAJOR + "." + m_Consumer.VERSION_MINOR + "." + 
			m_Consumer.VERSION_PATCH);

		PrintWriter fileWriter = null;
		Calendar fileCreationDate = null;

		try {
			// create printwriter to write to disk
			fileWriter = createPrintWriter(fileName);

			// get current date as a calender
			fileCreationDate = Calendar
					.getInstance(TimeZone.getTimeZone("GMT"));

			// run until stopped
			while (true) {

				// get messages from broker, wait for a maximum of 10 seconds before 
				// giving up
				ArrayList<String> brokerMessages = m_Consumer.pollString(pollTimeout * 1000);

				// nullcheck brokerMessages
				if (brokerMessages == null) {
					continue;
				}

				if (brokerMessages.size() == 0) {
					continue;
				}

				// add all messages in brokerMessages to queue
				for (int i = 0; i < brokerMessages.size(); i++) {
					// get string
					String message = brokerMessages.get(i);

					// nullcheck
					if (message == null) {
						continue;
					}
					if (message.length() == 0) {
						continue;
					}

					logger.debug(message);

					// check to see if we were newline terminated, add a
					// newline if we were not
					if (message.charAt(message.length() - 1) != '\n') {
						message = message.concat("\n");
					}

					// just call print
					fileWriter.print(message);
				}
				
				// make sure all messages written to disk
				fileWriter.flush();
				logger.info("Updated Archive File: " + outFileName + " with " 
					+ String.valueOf(brokerMessages.size()) + " additional message(s).");

				// get current date
				Calendar currentDate = Calendar
						.getInstance(TimeZone.getTimeZone("GMT"));

				// check to see if the date changed
				if (currentDate.get(Calendar.DAY_OF_YEAR) > fileCreationDate
						.get(Calendar.DAY_OF_YEAR)) {
					
					// close the current file
					fileWriter.close();

					// create new file for the new day
					fileWriter = createPrintWriter(fileName);

					// get the new creation date
					fileCreationDate = currentDate;

					logger.info("Switched to new Archive File: " + outFileName + ".");
				}
			}
		} catch (Exception e) {

			// log exception
			logger.error(e.toString());

		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
	}

	public static PrintWriter createPrintWriter(String name)
			throws IOException {
		
		// build filename from desired output directory, time, optional
		// name, and extension
		outFileName = "";
		if (name != "") {
			outFileName = outputDirectory + "/" + getUTCDateAsString() + "_"
					+ name + "." + fileExtension;
		} else {
			outFileName = outputDirectory + "/" + getUTCDateAsString() + "."
					+ fileExtension;
		}

		// create a printwriter to write to disk
		return (new PrintWriter(
				new BufferedWriter(new FileWriter(outFileName, true))));
	}

	public static String getUTCDateAsString() {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String utcTime = sdf.format(new Date());

		return utcTime;
	}

}
