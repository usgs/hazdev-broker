package gov.usgs.hazdevbroker;

import java.util.*;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import org.apache.log4j.Logger;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * A hazdev broker class used to poll data from the hazdev kafka broker cluster.
 *
 * @author U.S. Geological Survey &lt;jpatton at usgs.gov&gt;
 */
public class Consumer extends ClientBase {

	/**
	 * Optional configuration string defining the heartbeat directory
	 */
	private static String heartbeatDirectory;

	/**
	 * The kafka consumer client
	 */
	private static org.apache.kafka.clients.consumer.Consumer<String, byte[]> consumer;

	/**
	 * The Heartbeat message processor
	 */
	private static Heartbeat heartbeatProcessor;

	/**
	 * Variable containing time of the last heartbeat.
	 */
	private static Long lastHeartbeatTime;

	/**
	 * A collection of strings contining the topics 
	 */	
	private static Collection<String> topicList;

	/**
	 * Log4J logger for Consumer
	 */
	static Logger logger = Logger.getLogger(Consumer.class);

	/**
	 * The constructor for the Consumer class. Initializes members to default or
	 * null values.
	 */
	public Consumer() {
		// init
		consumer = null;
		heartbeatProcessor = null;
		heartbeatDirectory = null;
		topicList = null;
		CONFIGTYPE_STRING = "ConsumerConfig";

		// init last  heartbeat time to now
		lastHeartbeatTime = (Long) (System.currentTimeMillis() / 1000);	
	}

	/**
	 * The advanced constructor for the Consumer class. Initializes members to
	 * default values and used the provided JSON configuration to configure the
	 * kafka consumer client.
	 *
	 * @param configObject
	 *            - A JSONObject containing the configuration
	 */
	public Consumer(JSONObject configObject) {
		// init
		consumer = null;
		heartbeatProcessor = null;
		heartbeatDirectory = null;
		topicList = null;
		CONFIGTYPE_STRING = "ConsumerConfig";

		// init last  heartbeat time to now
		lastHeartbeatTime = (Long) (System.currentTimeMillis() / 1000);		

		// configuration/setup
		Properties configuration = convertJSONConfigToProp(configObject);
		setup(configuration);
	}

/**
	 * The advanced constructor for the Consumer class. Initializes members to
	 * default values and used the provided JSON configuration to configure the
	 * kafka consumer client.
	 *
	 * @param configObject
	 *            - A JSONObject containing the configuration
	 * @param hbDirectory
	 * 			  - A String containing the directory to write heartbeat messages
	 * Set to null to disable heartbeat messages
	 */
	public Consumer(JSONObject configObject, String hbDirectory) {
		// init
		consumer = null;
		heartbeatProcessor = null;
		heartbeatDirectory = hbDirectory;
		topicList = null;
		CONFIGTYPE_STRING = "ConsumerConfig";

		// init last  heartbeat time to now
		lastHeartbeatTime = (Long) (System.currentTimeMillis() / 1000);		

		// configuration/setup
		Properties configuration = null;
		try {
			configuration = convertJSONConfigToProp(configObject);
		} catch (Exception e) {
			logger.error("Exception converting configuration: " + e.toString());
			throw (e);
		}

		setup(configuration);	
	}

	/**
	 * The advanced constructor for the Consumer class. Initializes members to
	 * default values and used the provided JSON configuration string to
	 * configure the kafka consumer client.
	 *
	 * @param configString
	 *            - A JSON formatted String containing the configuration
	 * @throws org.json.simple.parser.ParseException
	 *             if a json parse exception occurs
	 */
	public Consumer(String configString) throws ParseException {
		// init
		consumer = null;
		heartbeatProcessor = null;
		heartbeatDirectory = null;
		topicList = null;
		CONFIGTYPE_STRING = "ConsumerConfig";

		// init last  heartbeat time to now
		lastHeartbeatTime = (Long) (System.currentTimeMillis() / 1000);		

		// configuration/setup
		Properties configuration = null;
		try {
			configuration = convertJSONStringToProp(configString);
		} catch (ParseException e) {
			logger.error("ParseException converting configuration: " + e.toString());
			throw (e);
		}

		setup(configuration);	
	}

	/**
	 * The advanced constructor for the Consumer class. Initializes members to
	 * default values and used the provided JSON configuration string to
	 * configure the kafka consumer client.
	 *
	 * @param configString
	 *            - A JSON formatted String containing the configuration
	 * @param hbDirectory
	 * 			  - A String containing the directory to write heartbeat messages
	 * @throws org.json.simple.parser.ParseException
	 *             if a json parse exception occurs
	 */
	public Consumer(String configString, String hbDirectory) 
			throws ParseException {
		// init
		consumer = null;
		heartbeatProcessor = null;
		heartbeatDirectory = hbDirectory;
		topicList = null;
		CONFIGTYPE_STRING = "ConsumerConfig";

		// init last  heartbeat time to now
		lastHeartbeatTime = (Long) (System.currentTimeMillis() / 1000);		

		// configuration/setup
		Properties configuration = null;
		try {
			configuration = convertJSONStringToProp(configString);
		} catch (ParseException e) {
			logger.error("ParseException converting configuration: " + e.toString());
			throw (e);
		}

		setup(configuration);		
	}

	/**
	 * The setup function for the Consumer class. Uses the provided Properties 
	 * configuration to configure the kafka producer client.
	 *
	 * @param configProperties
	 *            - A Properties containing the configuration
	 * @return Returns true if successful, false otherwise.
	 */	
	public boolean setup(Properties configProperties) {

		if (configProperties == null) {
			return(false);
		}

		try {
			// add any fixed configuration (like the serializer
			configProperties.put("key.deserializer",
					"org.apache.kafka.common.serialization.StringDeserializer");
			configProperties.put("value.deserializer",
					"org.apache.kafka.common.serialization.ByteArrayDeserializer");

			// create the consumer
			consumer = new KafkaConsumer<String, byte[]>(configProperties);

			// create the heartbeat processor, we need this to tell if a 
			// message is a heartbeat or not
			heartbeatProcessor = new Heartbeat();	
		} catch (Exception e) {
			logger.error("Exception configuring consumer: " + e.toString());
			return(false);
		}

		return(true);
	}

	/**
	 * Subscribes the kafka consumer client to the provided topic
	 *
	 * @param topic
	 *            - A String containing the topic to subscribe to.
	 */
	public void subscribe(String topic) {
		subscribe(Arrays.asList(topic));
	}

	/**
	 * Subscribes the kafka consumer client to the provided list of topics
	 *
	 * @param topics
	 *            - A Collection&lt;String&gt; containing the list of topics to
	 *            subscribe to.
	 */
	public void subscribe(Collection<String> topics) {
		consumer.subscribe(topics);

		// remember the topic list for handling heartbeats
		topicList = topics;
	}

	/**
	 * Polls the hazdev kafka broker cluster for data.
	 *
	 * @param timeout
	 *            - A long containing the time to wait while polling in
	 *            milliseconds. -1 indicates that the client should wait for an
	 *            arbitrarily long time
	 * @return Returns an ArrayList&lt;byte[]&gt; containing the data from the broker
	 *         cluster since the last time it was polled.
	 */
	public ArrayList<byte[]> poll(long timeout) {

		ArrayList<byte[]> data = new ArrayList<byte[]>();

		// Negative value means wait for an arbitrarily long time
		if (timeout < 0) {
			timeout = Long.MAX_VALUE;
		}

		// get any messages pending for our topic(s) from kafka
		ConsumerRecords<String, byte[]> records = null;
		try {
			records = consumer.poll(timeout);
		} catch (Exception e) { 
			logger.error("Error calling consumer.poll: " + e.toString());
			return (null);
		}

		// nullcheck
		if (records == null) {
			return(null);
		}

		// go though each message, adding it to the return ArrayList
		// removing heartbeat messages
		for (ConsumerRecord<String, byte[]> record : records) {
			// nullcheck
			if (record == null) {
				continue;
			}

			// convert to string to see if this is a heartbeat
			// note that if poll was called by pollString, we're
			// converting *twice*, I'm not sure how to check for heartbeats
			// more efficiently than this tho
			String recordString = new String(record.value());

			// don't add heartbeats to the data arraylist
			if (heartbeatProcessor.fromJSONString(recordString) == true) {
				handleHeartbeat(heartbeatProcessor);
			} else {
				data.add(record.value());
			}
		}

		return (data);
	}

	/**
	 * Polls the hazdev kafka broker cluster for messages.
	 *
	 * @param timeout
	 *            - A long containing the time to wait while polling in
	 *            milliseconds. -1 indicates that the client should wait for an
	 *            arbitrarily long time
	 * @return Returns an ArrayList&lt;String&gt; containing the messages from the
	 *         broker cluster since the last time it was polled.
	 */
	public ArrayList<String> pollString(long timeout) {

		ArrayList<String> messages = new ArrayList<String>();

		// get data
		ArrayList<byte[]> data = poll(timeout);

		// nullcheck
		if (data == null) {
			return(null);
		}

		// convert bytes to strings
		for (byte[] aData : data) {
			// nullcheck
			if (aData == null) {
				continue;
			}
			
			messages.add(new String(aData));
		}

		return (messages);
	}

	/**
	 * heartbeat handling function
	 *
	 * @param aHeartbeat
	 *            - A Heartbeat containing the heartbeat to handle
	 */	
	public void handleHeartbeat(Heartbeat aHeartbeat) {

		// is this a valid heartbeat
		if (aHeartbeat.isValid() == false) {
			return;
		}

		// is this heartbeat for one of the configured topics
		// we don't want to handle hearbeats for other topics
		if (!topicList.contains(aHeartbeat.getTopic())) {
			return;
		}

		// set the time the heartbeat was received in case our 
		// caller is monitoring this
		setLastHeartbeatTime(System.currentTimeMillis() / 1000);

		// write the heartbeat to disk (won't write if heartbeatDirectory is
		// null)
		try {
			aHeartbeat.writeToDisk(heartbeatDirectory);
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException " + e.toString());
			return;
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException " + e.toString());
			return;
		}  
	}

	/**
	 * this function gets the partition ids for a topic
	 *
	 * @param topic
	 *            - A string containing the topic to query
	 */	
	public ArrayList<String> getPartitions(String topic) {
		ArrayList<String> partitions = new ArrayList<String>();

		if (consumer == null) {
			return partitions;
		}

		try{
			List<PartitionInfo> partitionInfo = consumer.partitionsFor(topic);

			for (int i = 0; i < partitionInfo.size(); i++) {
				PartitionInfo aPartition = partitionInfo.get(i);
				partitions.add(String.valueOf(aPartition.partition()));
			}
		} catch (Exception e) {
		}

		return partitions;
	}

	/**
	 * @return the lastHeartbeatTime
	 */
	public Long getLastHeartbeatTime() {
		return lastHeartbeatTime;
	}

	/**
	 * @param lastHeartbeatTime
	 *            the lastHeartbeatTime to set
	 */
	public void setLastHeartbeatTime(Long lastHeartbeatTime) {
		this.lastHeartbeatTime = lastHeartbeatTime;
	}  
}
