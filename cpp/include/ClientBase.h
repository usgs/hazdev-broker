/*****************************************
 * This file is documented for Doxygen.
 * If you modify this file please update
 * the comments so that Doxygen will still
 * be able to work.
 ****************************************/
#ifndef HAZDEVBROKER_CLIENTBASE_H
#define HAZDEVBROKER_CLIENTBASE_H

#include <Utility.h>

#include <rdkafkacpp.h>

#include <string>
#include <exception>
#include <functional>
#include <iostream>
#include <cstdlib>
#include <cstdio>
#include <csignal>
#include <cstring>

namespace hazdevbroker {
/**
 * \brief hazdevbroker client base class
 *
 * The hazdevbroker client base class is a class used to provide logging
 * functionality and JSON configuration parsing.  This class uses
 * rapidjson for JSON, and librdkafa for kafka functionality.
 */
class ClientBase {
 public:
	/**
	 * \brief ClientBase constructor
	 *
	 * The constructor for the ClientBase class.
	 * Initializes members to default values.
	 */
	ClientBase();

	/**
	 * \brief ClientBase destructor
	 *
	 * The destructor for the ClientBase class.
	 */
	~ClientBase();

	/**
	 * \brief virtual JSON setup function
	 *
	 * \param configJSON - A reference to a rapidjson::Value containing the
	 * configuration.
	 * \param topicConfigJSON - A reference to a rapidjson::Value containing the
	 * topic configuration.
	 */
	virtual void setup(rapidjson::Value &configJSON, // NOLINT
			rapidjson::Value &topicConfigJSON) = 0; // NOLINT

	/**
	 * \brief virtual string setup function
	 *
	 * \param configString - A json formatted std::string containing the
	 * configuration.
	 * \param topicConfigString - A json formatted std::string containing the
	 * topic configuration.
	 */
	virtual void setup(std::string configString,
			std::string topicConfigString) = 0;

	/**
	 * \brief string to kafka configuration function
	 *
	 * Converts the provided json formatted string into a pointer to a populated
	 * RdKafka::Conf object
	 *
	 * \param configString - A json formatted std::string containing the
	 * configuration.
	 * \param topicConfigString - A json formatted std::string containing the
	 * topic configuration.
	 * \returns Returns a pointer to a populated RdKafka::Conf object if
	 * successful, NULL otherwise.
	 */
	RdKafka::Conf * convertJSONStringToProp(std::string configString,
			std::string topicConfigString);

	/**
	 * \brief JSON document to kafka configuration function
	 *
	 * Converts the provided json document into a pointer to a populated
	 * RdKafka::Conf object
	 *
	 * \param configJSON - A reference to a rapidjson::Value containing the
	 * configuration.
	 * \param topicConfigJSON - A reference to a rapidjson::Value containing the
	 * topic configuration.
	 * \returns Returns a pointer to a populated RdKafka::Conf object if
	 * successful, NULL otherwise.
	 */
	RdKafka::Conf * convertJSONConfigToProp(rapidjson::Value &configJSON,  // NOLINT
			rapidjson::Value &topicConfigJSON);  // NOLINT

	/**
	 * \brief optional logging callback setup function
	 *
	 * \param callback - A std::function<void(std::string)> containing the
	 * callback function
	 */
	void setLogCallback(std::function<void(const std::string &)> callback);

 protected:
	/**
	 * \brief logging function
	 *
	 * Function to log error and status messages.  If the optional logging
	 * callback is not set up, this function will log to stderr
	 * \param logMessage - A std::string containing the logging message
	 */
	void log(std::string logMessage);

	/**
	 * \brief a std::string containing the configuration type used by clients.
	 */
	std::string m_sConfigType;

	/**
	 * The a std::string containing the client id used by clients.
	 */	
	std::string m_sClientId;

 private:
	/**
	 * \brief A std::function<void(const std::string &)> containing the optional 
	 * logging callback.
	 */
	std::function<void(const std::string &)> m_logCallback;
};
}  // namespace hazdevbroker
#endif  // HAZDEVBROKER_CLIENTBASE_H
