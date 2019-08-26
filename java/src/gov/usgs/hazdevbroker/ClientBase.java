package gov.usgs.hazdevbroker;

import java.util.*;

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
	public static final Integer VERSION_MINOR = 2;
	public static final Integer VERSION_PATCH = 4; 

	/**
	 * Client Configuration ID
	 */
	public String CONFIGTYPE_STRING = "";

	/**
	 * Log4J logger for ClientBase
	 */
	static Logger baseLogger = Logger.getLogger(ClientBase.class);

	/**
	 * The constructor for the ClientBase class.
	 */
	public ClientBase() {
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
