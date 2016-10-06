/*****************************************
 * This file is documented for Doxygen.
 * If you modify this file please update
 * the comments so that Doxygen will still
 * be able to work.
 ****************************************/
#ifndef HAZDEVBROKER_CONSUMER_H
#define HAZDEVBROKER_CONSUMER_H

#include "ClientBase.h"

namespace hazdevbroker {
/**
 * \brief hazdevbroker consumer class
 *
 * The hazdevbroker consumer class is a class used to poll for data and/or
 * messages from a hazdev kafka broker cluster.  The class extends from
 * \see ClientBase to  get logging functionality and JSON parsing (rapidjson).
 * This class uses librdkafka for communication with kafka.
 */
class Consumer: public ClientBase {
public:

	/**
	 * \brief Consumer constructor
	 *
	 * The constructor for the Consumer class.
	 * Initializes members to default values.
	 */
	Consumer();

	/**
	 * \brief Consumer advanced constructor
	 *
	 * The advanced constructor for the Consumer class.
	 * Initilizes members to default values.
	 * Sets up the class and rdkafka consumer using the provided
	 * rapidjson::Document
	 *
	 * \param configJSON - A reference to a rapidjson::Value containing the
	 * comsumer configuration.
	 * \param topicConfigJSON - A reference to a rapidjson::Value containing the
	 * topic configuration.
	 */
	Consumer(rapidjson::Value &configJSON, rapidjson::Value &topicConfigJSON);

	/**
	 * \brief Consumer advanced constructor
	 *
	 * The advanced constructor for the Consumer class.
	 * Initializes members to default values.
	 * Sets up the class and rdkafka consumer using the provided
	 * json formatted string
	 *
	 * \param configString - A json formatted std::string containing the consumer
	 * configuration.
	 * \param topicConfigString - A json formatted std::string containing the
	 * topic configuration.
	 */
	Consumer(std::string configString, std::string topicConfigString);

	/**
	 * \brief Consumer destructor
	 *
	 * The destructor for the Consumer class.
	 */
	~Consumer();

	/**
	 * \brief Consumer setup function
	 *
	 * Sets up the class and creates the rdkafka consumer using the provided
	 * rapidjson::Document
	 *
	 * \param configJSON - A reference to a rapidjson::Value containing
	 * consumer configuration.
	 * \param topicConfigJSON - A reference to a rapidjson::Value containing the
	 * topic configuration.
	 */
	virtual void setup(rapidjson::Value &configJSON,
			rapidjson::Value &topicConfigJSON);

	/**
	 * \brief Consumer setup function
	 *
	 * Sets up the class and creates the rdkafka consumer using the provided
	 * json formatted string
	 *
	 * \param configString - A json formatted std::string containing the consumer
	 * configuration.
	 * \param topicConfigString - A json formatted std::string containing the
	 * topic configuration.
	 */
	virtual void setup(std::string configString, std::string topicConfigString);

	/**
	 * \brief Consumer topic subscription function
	 *
	 * Subscribes to the provided kafka topic
	 *
	 * \param topic - A std::string containing the name of the
	 * the kafka topic to subscribe to
	 * \return Returns true if successful, false otherwise
	 */
	bool subscribe(std::string topic);

	/**
	 * \brief Consumer topic subscription function
	 *
	 * Subscribes to the provided list of kafka topics
	 *
	 * \param topics - A std::vector<std::string> containing the names of the
	 * the kafka topics to subscribe to
	 * \return Returns true if successful, false otherwise
	 */
	bool subscribe(std::vector<std::string> topics);

	/**
	 * \brief Consumer binary data polling function
	 *
	 * Polls kafka for a binary data from the subscribed topics.
	 *
	 * \param timeout - A long containing the time to wait before returning in
	 * milliseconds, -1 indicates wait indefinitely
	 * \param datalength - A pointer a size_t variable that will contain the size
	 * of the received data
	 * \return Returns a byte pointer to the received data (size is in
	 * datalength), or NULL if no data received
	 */
	byte * poll(long timeout, size_t *datalength);

	/**
	 * \brief Consumer string polling function
	 *
	 * Polls kafka for a string message from the subscribed topics.
	 *
	 * \param timeout - A long containing the time to wait before returning in
	 * milliseconds, -1 indicates wait indefinitely
	 * \return The std::string containing the received message, or empty string
	 * if no message received
	 */
	std::string pollString(long timeout);

private:

	/**
	 * \brief rdkafka consumer pointer
	 *
	 * The RdKafka::KafkaConsumer * variable
	 */
	RdKafka::KafkaConsumer * m_pConsumer;

};
}
#endif
