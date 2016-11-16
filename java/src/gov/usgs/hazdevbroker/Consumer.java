package gov.usgs.hazdevbroker;

import java.util.*;

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
	 * The kafka consumer client
	 */
	private static org.apache.kafka.clients.consumer.Consumer<String, byte[]> consumer;

	/**
	 * The constructor for the Consumer class. Initializes members to default or
	 * null values.
	 */
	public Consumer() {
		// init
		consumer = null;
		CONFIGTYPE_STRING = "ConsumerConfig";
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
		CONFIGTYPE_STRING = "ConsumerConfig";

		// configuration
		Properties configuration = convertJSONConfigToProp(configObject);
		if (configuration == null) {
			return;
		}

		// add any fixed configuration (like the serializer
		configuration.put("key.deserializer",
				"org.apache.kafka.common.serialization.StringDeserializer");
		configuration.put("value.deserializer",
				"org.apache.kafka.common.serialization.ByteArrayDeserializer");

		// create the consumer
		consumer = new KafkaConsumer<String, byte[]>(configuration);
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
		CONFIGTYPE_STRING = "ConsumerConfig";

		// configuration
		Properties configuration = convertJSONStringToProp(configString);
		if (configuration == null) {
			return;
		}

		// add any fixed configuration (like the serializer
		configuration.put("key.deserializer",
				"org.apache.kafka.common.serialization.StringDeserializer");
		configuration.put("value.deserializer",
				"org.apache.kafka.common.serialization.ByteArrayDeserializer");

		// create the consumer
		consumer = new KafkaConsumer<String, byte[]>(configuration);
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
		ConsumerRecords<String, byte[]> records = consumer.poll(timeout);
		for (ConsumerRecord<String, byte[]> record : records) {
			data.add(record.value());
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

		// convert bytes to strings
		for (byte[] aData : data) {
			messages.add(new String(aData));
		}

		return (messages);
	}
}
