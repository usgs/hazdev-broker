#include <project_version.h>
#include <ClientBase.h>
#include <rdkafkacpp.h>
#include <limits>
#include <string>

// JSON Keys
#define TYPE_KEY "Type"
#define PROPERTIES_KEY "Properties"
#define TOPICTYPE_STRING "TopicConfig"
#define CLIENT_ID "client.id"

namespace hazdevbroker {

ClientBase::ClientBase() {
	m_sConfigType = "";
	log("hazdev-broker: v" +
		std::to_string(PROJECT_VERSION_MAJOR) + "." +
		std::to_string(PROJECT_VERSION_MINOR) + "." +
		std::to_string(PROJECT_VERSION_PATCH));
}

ClientBase::~ClientBase() {
	/*
	 * Wait for RdKafka to decommission.
	 * This is not strictly needed (with check outq_len() above), but
	 * allows RdKafka to clean up all its resources before the application
	 * exits so that memory profilers such as valgrind wont complain about
	 * memory leaks.
	 */
	RdKafka::wait_destroyed(5000);
}

RdKafka::Conf * ClientBase::convertJSONStringToProp(std::string configString,
		std::string topicConfigString) {
	// use a document parse to convert from a string
	rapidjson::Document configJSON;
	if (configJSON.Parse(configString.c_str()).HasParseError()) {
		log("ClientBase::convertJSONStringToProp(): Error Parsing config string to"
		" JSON.");
		return (NULL);
	}

	rapidjson::Document topicConfigJSON;
	if (topicConfigString != "") {
		if (topicConfigJSON.Parse(topicConfigString.c_str()).HasParseError()) {
			log("ClientBase::convertJSONStringToProp(): Error Parsing topic config"
			" string to JSON.");
			return (NULL);
		}
	}

	return (convertJSONConfigToProp(configJSON, topicConfigJSON));
}

RdKafka::Conf * ClientBase::convertJSONConfigToProp(
		rapidjson::Value &configJSON, rapidjson::Value &topicConfigJSON) {
	std::string errstr;

	// check the type to ensure that this configuration is for the
	// the correct type
	if ((configJSON.HasMember(TYPE_KEY) == true)
			&& (configJSON[TYPE_KEY].IsString() == true)) {
		std::string configType = configJSON[TYPE_KEY].GetString();
		if (configType != m_sConfigType) {
			log("ClientBase::convertJSONConfigToProp(): Error, Configuration is "
			"not for: " + m_sConfigType + ", it is for: " + configType);
			return (NULL);
		}
	} else {
		log("Error, " + std::string(TYPE_KEY) + " missing from configuration.");
		return (NULL);
	}

	// check for properties object
	if (configJSON.HasMember(PROPERTIES_KEY) == false) {
		log("ClientBase::convertJSONConfigToProp(): Error, " +
			std::string(PROPERTIES_KEY) + " missing from configuration.");
		return (NULL);
	} else if (configJSON[PROPERTIES_KEY].IsObject() == false) {
		log("ClientBase::convertJSONConfigToProp(): Error, " +
			std::string(PROPERTIES_KEY) + " is not an object.");
		return (NULL);
	}

	// get the properties object
	rapidjson::Value & propertiesObject = configJSON[PROPERTIES_KEY];

	//  Create configuration objects
	RdKafka::Conf *conf = RdKafka::Conf::create(RdKafka::Conf::CONF_GLOBAL);

	// add all the key/values in the properties object to the config
	for (rapidjson::Value::ConstMemberIterator itr =
			propertiesObject.MemberBegin(); itr != propertiesObject.MemberEnd();
			++itr) {
		// remember the client id for later
		if (strncmp(itr->name.GetString(), CLIENT_ID, sizeof(CLIENT_ID)) == 0) {
			m_sClientId = itr->value.GetString();
		}

		if (conf->set(itr->name.GetString(), itr->value.GetString(), errstr)
				!= RdKafka::Conf::CONF_OK) {
			log("ClientBase::convertJSONConfigToProp:(): Error setting "
				"configuration entry: " +
				std::string(itr->name.GetString()) + " value: " +
				std::string(itr->value.GetString()) + " error: " +
				errstr);
			return (NULL);
		}
	}

	// topic
	RdKafka::Conf *topicConfig = RdKafka::Conf::create(RdKafka::Conf::CONF_TOPIC);

	// were we given one
	if (topicConfigJSON.IsObject() == true) {
		// check the type to ensure that this configuration is for the
		// topic
		if ((topicConfigJSON.HasMember(TYPE_KEY) == true)
				&& (topicConfigJSON[TYPE_KEY].IsString() == true)) {
			std::string topicConfigType = topicConfigJSON[TYPE_KEY].GetString();
			if (topicConfigType != std::string(TOPICTYPE_STRING)) {
				log("ClientBase::convertJSONConfigToProp:(): Error, Configuration "
					"is not for: " + std::string(TOPICTYPE_STRING)
					+ ", it is for: " + topicConfigType);
				return (NULL);
			}
		} else {
			log("ClientBase::convertJSONConfigToProp(): Error, " +
				std::string(TYPE_KEY) + " missing from topic configuration.");
			return (NULL);
		}

		// check for properties object
		if (topicConfigJSON.HasMember(PROPERTIES_KEY) == false) {
			log("ClientBase::convertJSONConfigToProp(): Error, " +
				std::string(PROPERTIES_KEY) + " missing from topic "
				"configuration.");
			return (NULL);
		} else if (topicConfigJSON[PROPERTIES_KEY].IsObject() == false) {
			log("Error, " + std::string(PROPERTIES_KEY) + " is not an object.");
			return (NULL);
		}

		// get the topic properties object
		rapidjson::Value & topicPropertiesObject =
				topicConfigJSON[PROPERTIES_KEY];

		// add all the key/values in the topic properties object to the config
		for (rapidjson::Value::ConstMemberIterator itr =
				topicPropertiesObject.MemberBegin();
				itr != topicPropertiesObject.MemberEnd(); ++itr) {
			if (topicConfig->set(itr->name.GetString(), itr->value.GetString(),
					errstr) != RdKafka::Conf::CONF_OK) {
				log("ClientBase::convertJSONConfigToProp(): Error setting topic "
					"configuration entry: " +
					std::string(itr->name.GetString()) + " value: " +
					std::string(itr->value.GetString()) + " error: " +
					errstr);
				return (NULL);
			}
		}
	} else {
		log("ClientBase::convertJSONConfigToProp(): Using default topic "
			"configuration");
	}

	// set the topic config into the overall config
	if (conf->set("default_topic_conf", topicConfig, errstr)
			!= RdKafka::Conf::CONF_OK) {
		log("ClientBase::convertJSONConfigToProp(): Error setting default topic "
			"configuration entry: " + errstr);
		return (NULL);
	}
	delete (topicConfig);

	return (conf);
}

void ClientBase::setLogCallback(
		std::function<void(const std::string &)> callback) {
	m_logCallback = callback;
}

void ClientBase::log(std::string logMessage) {
	// use the callback if it's available
	if (m_logCallback) {
		m_logCallback(logMessage);
	} else {
		std::cerr << logMessage.c_str() << std::endl;
	}
}
}  // namespace hazdevbroker
