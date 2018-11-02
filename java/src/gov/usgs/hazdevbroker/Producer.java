package gov.usgs.hazdevbroker;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
	 * The client id for this producer
	 */	
	public String clientId;

	/**
	 * The constructor for the Producer class. Initializes members to default or
	 * null values.
	 */
	public Producer() {
		producer = null;
		clientId = null;
		CONFIGTYPE_STRING = "ProducerConfig";
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
		// init
		producer = null;
		clientId = null;
		CONFIGTYPE_STRING = "ProducerConfig";

		// configuration
		Properties configuration = convertJSONConfigToProp(configObject);
		if (configuration == null) {
			return;
		}

		// build client id
		if (configuration.getProperty("client.id") != null) {
			clientId = configuration.getProperty("client.id");
		}

		// add any fixed configuration (like the serializer)
		configuration.put("key.serializer",
				"org.apache.kafka.common.serialization.StringSerializer");
		configuration.put("value.serializer",
				"org.apache.kafka.common.serialization.ByteArraySerializer");

		// create the producer
		producer = new KafkaProducer<String, byte[]>(configuration);
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
	public Producer(String configString) throws ParseException {
		// init
		producer = null;
		clientId = null;
		CONFIGTYPE_STRING = "ProducerConfig";

		// configuration
		Properties configuration = convertJSONStringToProp(configString);
		if (configuration == null) {
			return;
		}

		// build client id
		if (configuration.getProperty("client.id") != null) {
			clientId = configuration.getProperty("client.id");
		}

		// add any fixed configuration (like the serializer
		configuration.put("key.serializer",
				"org.apache.kafka.common.serialization.StringSerializer");
		configuration.put("value.serializer",
				"org.apache.kafka.common.serialization.ByteArraySerializer");

		// create the producer
		producer = new KafkaProducer<String, byte[]>(configuration);
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
		producer.send(message);
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
	 * Generates and sends a heartbeat message to the hazdev kafka broker
	 * cluster using the provided topic
	 *
	 * @param topic
	 *            - A String containing the topic to send to
	 */
	public void sendHeartbeat(String topic) {

		// create the heartbeat
		Heartbeat newHeartbeat = new Heartbeat(new Date(), topic, clientId);

		// send the heartbeat
		if (newHeartbeat.isValid()) {		
			sendString(topic, newHeartbeat.toJSONString());
		}
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
}
