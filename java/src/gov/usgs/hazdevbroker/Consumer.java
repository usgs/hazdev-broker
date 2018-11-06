package gov.usgs.hazdevbroker;

import java.util.*;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.apache.kafka.clients.consumer.*;
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
		Properties configuration = convertJSONConfigToProp(configObject);
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
		Properties configuration = convertJSONStringToProp(configString);
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
		Properties configuration = convertJSONStringToProp(configString);
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
	 * @throws org.json.simple.parser.ParseException
	 *             if a heartbeat parse exception occurs
	 * @throws java.io.FileNotFoundException 
	 * 			   if a heartbeat file could not be created 
	 * @throws java.io.UnsupportedEncodingException
	 *             if an encoding error occured
	 */
	public ArrayList<byte[]> poll(long timeout) throws ParseException,
			FileNotFoundException, UnsupportedEncodingException {

		ArrayList<byte[]> data = new ArrayList<byte[]>();

		// Negative value means wait for an arbitrarily long time
		if (timeout < 0) {
			timeout = Long.MAX_VALUE;
		}

		// get any messages pending for our topic(s) from kafka
		ConsumerRecords<String, byte[]> records = consumer.poll(timeout);

		// go though each message, adding it to the return ArrayList
		// removing heartbeat messages
		for (ConsumerRecord<String, byte[]> record : records) {

			// convert to string to see if this is a heartbeat
			// note that if poll was called by pollString, we're
			// converting *twice*, I'm not sure how to check for heartbeats
			// more efficenty than this tho
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
	 * @throws org.json.simple.parser.ParseException
	 *             if a heartbeat parse exception occurs
	 * @throws java.io.FileNotFoundException 
	 * 			   if a heartbeat file could not be created 
	 * @throws java.io.UnsupportedEncodingException
	 *             if an encoding error occured
	 */
	public ArrayList<String> pollString(long timeout) throws ParseException,
			FileNotFoundException, UnsupportedEncodingException {

		ArrayList<String> messages = new ArrayList<String>();

		// get data
		ArrayList<byte[]> data = poll(timeout);

		// convert bytes to strings
		for (byte[] aData : data) {
			messages.add(new String(aData));
		}

		return (messages);
	}

	/**
	 * heartbeat handling function
	 *
	 * @param aHeartbeat
	 *            - A Heartbeat containing the heartbeat to handle
	 * @throws java.io.FileNotFoundException 
	 * 			   if a heartbeat file could not be created 
	 * @throws java.io.UnsupportedEncodingException
	 *             if an encoding error occured
	 */	
	public void handleHeartbeat(Heartbeat aHeartbeat) throws 
			FileNotFoundException, UnsupportedEncodingException {

		// is this a valid heartbeat
		if (aHeartbeat.isValid() == false) {
			return;
		}

		// is this heartbeat for one of the configured topics
		// we don't want to handle hearbeats for other topics
		if (!topicList.contains(aHeartbeat.getTopic())) {
			return;
		}

		// set the time the heartbeat was recieved in case our 
		// caller is monitoring this
		setLastHeartbeatTime(System.currentTimeMillis() / 1000);

		// write the heartbeat to disk (won't write if heartbeatDirectory is
		// null)
		aHeartbeat.writeToDisk(heartbeatDirectory);
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
