package gov.usgs.hazdevbroker;

import java.util.*;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanInfo;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.AttributeList;
import java.beans.IntrospectionException;
import java.lang.management.ManagementFactory;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.log4j.Logger;

/**
 * A base class for hazdev broker clients, providing conversion functions
 * between strings, json objects, and properties
 *
 * @author U.S. Geological Survey &lt;jpatton at usgs.gov&gt;
 */
public class ClientBase {

	/**
	 * JSON Keys
	 */
	public static final String TYPE_KEY = "Type";
	public static final String PROPERTIES_KEY = "Properties";

	/** 
	 * Version
	 * NOTE: Make sure to also update the c++ version in version.cmake when 
	 * updating this file
	 */
	public static final Integer VERSION_MAJOR = 0;
	public static final Integer VERSION_MINOR = 3;
	public static final Integer VERSION_PATCH = 0; 

	/**
	 * Client Configuration ID
	 */
	public String CONFIGTYPE_STRING = "";

	/**
	 * Log4J logger for ClientBase
	 */
	static Logger baseLogger = Logger.getLogger(ClientBase.class);

	private MBeanServer kafkaMetrics = null;

	/**
	 * The constructor for the ClientBase class.
	 */
	public ClientBase() {
	}

	/**
	 * A function that pulls a kafka metric or metrics from JMX
	 *
	 * @param metricName
	 *            - A formatted String containing the name of the metric desired
	 * @param metricAttribute
	 *            - A String containing the name of the desired attribute of the metric
	 * 				if empty, returns all attributes for this metric
	 * @return Returns an ArrayList containing the metric values as Strings
	 */
	public ArrayList<String> getKafkaMetric(String metricName, String metricAttribute) {
		
		ArrayList<String> metrics = new ArrayList<String>();
		if (metricName == null) {
			return metrics;
		}
		if (kafkaMetrics == null) {
			kafkaMetrics = ManagementFactory.getPlatformMBeanServer();
		}

		try {
			ObjectName objName = new ObjectName(metricName);

			if (metricAttribute == "") {
				MBeanInfo info = kafkaMetrics.getMBeanInfo(objName);
				MBeanAttributeInfo[] attrs = info.getAttributes();
				
				// we don't have a specific metric, so get all of them
				for (int i = 0; i < attrs.length; i++) {
					Object anAttribute = kafkaMetrics.getAttribute(objName, attrs[i].getName());
					
					metrics.add(attrs[i].getName() + "=" + anAttribute.toString());
				}
			} else {
				Object anAttribute = kafkaMetrics.getAttribute(objName, metricAttribute);
				metrics.add(metricAttribute + "=" + anAttribute.toString());	
			}
		} catch (Exception e) {
		}

		return metrics;
	}

	/**
	 * A function that converts a JSON formatted string into a populated java
	 * Properties object.
	 *
	 * @param configString
	 *            - A JSON formatted String containing the configuration
	 * @return Returns a Properties object containing the configuration if
	 *         successful, null otherwise.
	 * @throws org.json.simple.parser.ParseException
	 *             if a json parse exception occurs
	 */
	public Properties convertJSONStringToProp(String configString)
			throws ParseException {

		if (configString == null) {
			baseLogger.error("Null configuration string.");
			return (null);
		}

		if ("".equals(configString)) {
			baseLogger.error("Empty configuration string.");
			return (null);
		}

		// convert from a string
		JSONObject configObject = Utility.fromJSONString(configString);

		return (convertJSONConfigToProp(configObject));
	}

	/**
	 * A function that converts a JSONObject into a populated java Properties
	 * object.
	 *
	 * @param configObject
	 *            - A JSONObject containing the configuration
	 * @return Returns a Properties object containing the configuration if
	 *         successful, null otherwise.
	 */
	public Properties convertJSONConfigToProp(JSONObject configObject) {

		// create properties object
		Properties configuration = new Properties();

		// check the type to ensure that this configuration is for the
		// producer
		if (configObject.containsKey(TYPE_KEY)) {
			if (!configObject.get(TYPE_KEY).toString()
					.equals(CONFIGTYPE_STRING)) {
				baseLogger.error("Wrong configuration type.");
				return (null);
			}
		} else {
			baseLogger.error("Missing configuration type.");
			return (null);
		}

		// check for properties key
		if (!configObject.containsKey(PROPERTIES_KEY)) {
			baseLogger.error("Missing properties.");
			return (null);
		}

		// get the properties object
		JSONObject propertiesObject = (JSONObject) configObject
				.get(PROPERTIES_KEY);

		// add all the key/values in the properties object to the config
		for (Object key : propertiesObject.keySet()) {

			// get the key and the value
			String keyStr = (String) key;
			Object keyvalue = propertiesObject.get(keyStr);

			// add to configuration
			configuration.put(keyStr, keyvalue);
		}

		return (configuration);
	}

}
