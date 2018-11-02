package gov.usgs.hazdevbroker;

import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A class for hazdev broker used to generate, parse, and check heartbeat 
 * messages.
 *
 * @author U.S. Geological Survey &lt;jpatton at usgs.gov&gt;
 */
public class Heartbeat {

	/**
	 * JSON Keys
	 */
    public static final String TYPE_KEY = "Type";
    public static final String HEARTBEAT_TYPE = "Heartbeat";
    public static final String TIME_KEY = "Time";  
    public static final String TOPIC_KEY = "Topic";
    public static final String CLIENTID_KEY = "ClientId";   
   
    /**
	 * The time of this heartbeat
	 */
    private Date time;
    
	/**
	 * The topic for this heartbeat
	 */
    private String topic;
    
    /**
	 * The Client ID for this heartbeat
	 */
	private String clientId;

    /**
	 * The constructor for the Heartbeat class. Initializes members to null 
     * values.
	 */
	public Heartbeat() {

        time = null;
		topic = null;
		clientId = null;       
    }    
    
    /**
	 * Advanced constructor
	 *
	 * The alternate advanced constructor for the Heartbeat class. Initializes
	 * members to provided values.
     * 
     * @param newTime
	 *            - A Date containing the new origin time to use
	 * @param newTopic
	 *            - A String containing the topic for this heartbeat
	 * @param newClientId
	 *            - A String containing the client id for this heartbeat
	 */
	public Heartbeat(Date newTime, String newTopic, String newClientId) {

        reload(newTime, newTopic, newClientId);   
    }  

    /**
	 * Reload Function
	 *
	 * The reload function for the Heartbeat class. Initializes members to 
	 * provided values.
	 *
	 * @param newTime
	 *            - A Date containing the new origin time to use
	 * @param newTopic
	 *            - A String containing the topic for this heartbeat
	 * @param newClientId
	 *            - A String containing the client id for this heartbeat
	 */
	public void reload(Date newTime, String newTopic, String newClientId) {

		time = newTime;
		topic = newTopic;
		clientId = newClientId;
	}

    /**
	 * Converts the contents of the class to a json string
	 *
	 * @return Returns a String containing the class contents formatted in 
     * json
	 */
    @SuppressWarnings("unchecked")
    public String toJSONString() {
        JSONObject newJSONObject = new JSONObject();

        Date jsonTime = getTime();
		String jsonTopic = getTopic();
		String jsonClientId = getClientId();
        
        // type
        newJSONObject.put(TYPE_KEY, HEARTBEAT_TYPE);

        // time
        if (jsonTime != null) {
            newJSONObject.put(TIME_KEY, Utility.formatDate(jsonTime));
        }

        // topic
        if (jsonTopic != null) {
            newJSONObject.put(TOPIC_KEY, jsonTopic);
        }

        // client id
        if (jsonClientId != null) {
            newJSONObject.put(CLIENTID_KEY, jsonClientId);
        }

		return(Utility.toJSONString(newJSONObject));
    }
    
	/** Converts the provided string from a serialized JSON string, populating
	 * members
	 * @param jsonString - A string containing the serialized JSON
	 * @return Returns true if successful, false otherwise
	 * @throws ParseException if one occurs
	 */    
    public boolean fromJSONString(String jsonString) throws ParseException {

        // convert from a string
        JSONObject newJSONObject = Utility.fromJSONString(jsonString);        

        // type
        if (newJSONObject.containsKey(TYPE_KEY)) {
            String type = newJSONObject.get(TYPE_KEY).toString();
            if (!type.equals(HEARTBEAT_TYPE)) {
                return(false);
            }
        } else {
            return(false);
        }
        
        // time
        if (newJSONObject.containsKey(TIME_KEY)) {
            time = Utility.getDate(newJSONObject.get(TIME_KEY).toString());
        } else {
            time = null;
        }

         // topic
		if (newJSONObject.containsKey(TOPIC_KEY)) {
			topic = newJSONObject.get(TOPIC_KEY).toString();
		} else {
			topic = null;
		}
        
        // client id
        if (newJSONObject.containsKey(CLIENTID_KEY)) {
			clientId = newJSONObject.get(CLIENTID_KEY).toString();
		} else {
			clientId = null;
        }
        
        return(isValid());
    }

    /**
	 * Validates the class.
	 *
	 * @return Returns true if successful
	 */
	public boolean isValid() {
		if (getErrors() == null) {
			return (true);
		} else if (getErrors().size() == 0) {
			return (true);
		} else {
			return (false);
		}
	}    

    /**
	 * Gets any validation errors in the class.
	 *
	 * @return Returns a List&lt;String&gt; of any errors found
	 */
	public ArrayList<String> getErrors() {

        Date jsonTime = getTime();
		String jsonTopic = getTopic();
		String jsonClientId = getClientId();

		ArrayList<String> errorList = new ArrayList<String>();

		// Required Keys
		// time
		if (jsonTime == null) {
			// time not found
			errorList.add("No Time in Heartbeat Class.");
		}

		// topic
		if (jsonTopic == null) {
			// topic not found
			errorList.add("No Topic in Heartbeat Class.");
		} else if (jsonTopic.isEmpty()) {
			// topic empty
			errorList.add("Empty Topic in Heartbeat Class.");
		}

		// client id
		if (jsonClientId == null) {
			// client id not found
			errorList.add("No Client Id in Heartbeat Class.");
		} else if (jsonClientId.isEmpty()) {
			// client id empty
			errorList.add("Empty Client Id in Heartbeat Class.");
		}

		// done
		return (errorList);
	}

    /**
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @param topic
	 *            the topic to set
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 * @param clientId
	 *            the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
    }
    
	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(Date time) {
		this.time = time;
	}    
}