package gov.usgs.hazdevbroker;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

import org.apache.kafka.clients.producer.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * A hazdev broker class used to send data into the hazdev kafka broker cluster.
 *
 * @author U.S. Geological Survey &lt;jpatton at usgs.gov&gt;
 */
public class Producer extends ClientBase {

	/**
	 * The kafka producer client
	 */
	private static org.apache.kafka.clients.producer.Producer<String, byte[]> producer;

	/**
	 * Long defining the number seconds between sending heartbeat messages, 
	 * default is 30 seconds, set to -1 to always send heartbeat messages, set 
	 * to null to disable heartbeat messages
	 */
	private static Long heartbeatInterval;

	/**
	 * Variable containing time the last heartbeat was sent.
	 */
	private static Long lastHeartbeatTime;

	/**
	 * The client id for this producer
	 */	
	private static String clientId;
        
        /**
         * When true a heartbeat message also be sent with each send.  Default is true.
         */
        private boolean autoSendHeartbeat = true;

	/**
	 * Log4J logger for Producer
	 */
	static Logger logger = Logger.getLogger(Producer.class);

	/**
	 * The constructor for the Producer class. Initializes members to default or
	 * null values.
	 */
	public Producer() {
		producer = null;
		clientId = null;
		heartbeatInterval = null;
		CONFIGTYPE_STRING = "ProducerConfig";

		// init last heartbeat time to now
		lastHeartbeatTime = (Long) (System.currentTimeMillis() / 1000);
	}

	/**
	 * The advanced constructor for the Producer class. Initializes members to
	 * default values and used the provided JSON configuration to configure the
	 * kafka producer client.
	 *
	 * @param configObject
	 *            - A JSONObject containing the configuration
	 */
	public Producer(JSONObject configObject) {
		producer = null;
		clientId = null;
		heartbeatInterval = 30L;
		CONFIGTYPE_STRING = "ProducerConfig";

		// init last heartbeat time to now
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
	 * The advanced constructor for the Producer class. Initializes members to
	 * default values and used the provided JSON configuration to configure the
	 * kafka producer client.
	 *
	 * @param configObject
	 *            - A JSONObject containing the configuration
	 * @param hbInterval
	 *            - A Long containing the heartbeat interval to use, set to -1 
	 * to always send heartbeat messages, set to null to disable heartbeat 
	 * messages
	 */
	public Producer(JSONObject configObject, Long hbInterval) {
		producer = null;
		clientId = null;
		heartbeatInterval = hbInterval;
		CONFIGTYPE_STRING = "ProducerConfig";

		// init last heartbeat time to now
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
	 * The advanced constructor for the Producer class. Initializes members to
	 * default values and used the provided JSON configuration string to
	 * configure the kafka producer client.
	 *
	 * @param configString
	 *            - A JSON formatted String containing the configuration
	 * @throws org.json.simple.parser.ParseException
	 *             if a json parse exception occurs
	 */
	public Producer(String configString) 
		throws ParseException {
		// init
		producer = null;
		clientId = null;
		heartbeatInterval = 30L;
		CONFIGTYPE_STRING = "ProducerConfig";
		
		// init last heartbeat time to now
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
	 * The advanced constructor for the Producer class. Initializes members to
	 * default values and used the provided JSON configuration string to
	 * configure the kafka producer client.
	 *
	 * @param configString
	 *            - A JSON formatted String containing the configuration
	 * @param hbInterval
	 *            - A Long containing the heartbeat interval to use, set to -1 
	 * to always send heartbeat messages, set to null to disable heartbeat 
	 * messages
	 * @throws org.json.simple.parser.ParseException
	 *             if a json parse exception occurs
	 */
	public Producer(String configString, Long hbInterval) 
		throws ParseException {
		// init
		producer = null;
		clientId = null;
		heartbeatInterval = hbInterval;
		CONFIGTYPE_STRING = "ProducerConfig";
		
		// init last heartbeat time to now
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
	 * The setup function for the Producer class. Uses the provided Properties 
	 * configuration to configure the kafka producer client.
	 *
	 * @param configProperties
	 *            - A Properties containing the configuration
	 * @return Returns true if successful, false otherwise.
	 */	
	public boolean setup(Properties configProperties) {
	
		// configuration
		if (configProperties == null) {
			return (false);
		}

		try {
			// build client id
			if (configProperties.getProperty("client.id") != null) {
				clientId = configProperties.getProperty("client.id");
			}

			// add any fixed configuration (like the serializer)
			configProperties.put("key.serializer",
					"org.apache.kafka.common.serialization.StringSerializer");
					configProperties.put("value.serializer",
					"org.apache.kafka.common.serialization.ByteArraySerializer");

			// create the producer
			producer = new KafkaProducer<String, byte[]>(configProperties);
		} catch (Exception e) {
			logger.error("Exception configuring producer: " + e.toString());
			return(false);
		}
		return(true);
	}

	/**
	 * Sends the contents of the provided byte array to the hazdev kafka broker
	 * cluster using the provided topic
	 *
	 * @param topic
	 *            - A String containing the topic to send to
	 * @param data
	 *            - A byte[] containing the data to send
	 */
	public void send(String topic, byte[] data) {

		// create the producer record
		ProducerRecord<String, byte[]> message = new ProducerRecord<String, byte[]>(
				topic, data);

		// send it async
		try {
			producer.send(message);
		} catch (Exception e) { 
			logger.error("Error calling producer.send: " + e.toString());
			return ;
		}

		// send heartbeat message, will not send if heartbeats
		// are disabled, or if it has not been long enough to
		// send a heartbeat
                if (autoSendHeartbeat) {
                  sendHeartbeat(topic);	
                }
	}

	/**
	 * Generates and sends a heartbeat message to the hazdev kafka broker
	 * cluster using the provided topic. NOTE that it is considered best 
	 * practice that a continuously running producer add a call to this function
	 * to it's sending loop.
	 *
	 * @param topic
	 *            - A String containing the topic to send to
	 */
	public void sendHeartbeat(String topic) {

		// don't send heartbeat if it's disabled
		if (heartbeatInterval != null) {

			// get current time in seconds
			Long timeNow = System.currentTimeMillis() / 1000;

			// calculate elapsed time
			Long elapsedTime = timeNow - lastHeartbeatTime;

			// has it been long enough since the last heartbeat?
			// or are we always sending heartbeats?
			if ((elapsedTime >= heartbeatInterval) || (heartbeatInterval < 0)) {
				
				// create the heartbeat
				Heartbeat newHeartbeat = new Heartbeat(new Date(), topic, clientId);

				// send the heartbeat
				if (newHeartbeat.isValid()) {	
					String heartbeatString = newHeartbeat.toJSONString();
					byte[] heartbeatData = heartbeatString.getBytes();

					ProducerRecord<String, byte[]> heartbeatMessage = 
						new ProducerRecord<String, byte[]>(topic, heartbeatData);	

					// send it async
					try {
						producer.send(heartbeatMessage);
					} catch (Exception e) { 
						logger.error("Error calling producer.send for heartbeat: " + e.toString());
						return ;
					}
				}

				// remember heartbeat time
				setLastHeartbeatTime(timeNow);
			}
		}
	}

	/**
	 * Sends the contents of the provided string to the hazdev kafka broker
	 * cluster using the provided topic
	 *
	 * @param topic
	 *            - A String containing the topic to send to
	 * @param message
	 *            - A String containing the message to send
	 */
	public void sendString(String topic, String message) {

		// convert message to bytes
		byte[] data = message.getBytes();

		// send
		send(topic, data);
	}

	/**
	 * Function that closes down the kafka producer client
	 *
	 * @param timeout
	 *            - A long containing time in milliseconds to wait before
	 *            closing the kafka producer client
	 */
	public void close(long timeout) {
		producer.close(timeout, TimeUnit.MILLISECONDS);
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
        
        /**
         * Send an additional heartbeat message with each send if sendHeartbeat == true
         * @param sendHeartbeat boolean, true to send heartbeat
         */
        public void toggleSendingHeartbeat(boolean sendHeartbeat) {
                this.autoSendHeartbeat = sendHeartbeat;
        }
}
