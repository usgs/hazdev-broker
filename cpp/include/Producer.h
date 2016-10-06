/*****************************************
 * This file is documented for Doxygen.
 * If you modify this file please update
 * the comments so that Doxygen will still
 * be able to work.
 ****************************************/
#ifndef HAZDEVBROKER_PRODUCER_H
#define HAZDEVBROKER_PRODUCER_H

#include "ClientBase.h"

namespace hazdevbroker {
/**
 * \brief hazdevbroker producer class
 *
 * The hazdevbroker producer class is a class used to send data and/or messages
 * into a hazdev kafka broker cluster.  The class extends from \see ClientBase
 * to get logging functionality and JSON parsing (rapidjson).  This class uses
 * librdkafka for communication with kafka.
 */
class Producer: public ClientBase {
public:

	/**
	 * \brief Producer constructor
	 *
	 * The constructor for the Producer class.
	 * Initializes members to default values.
	 */
	Producer();

	/**
	 * \brief Producer advanced constructor
	 *
	 * The advanced constructor for the Producer class.
	 * Initializes members to default values.
	 * Sets up the class and rdkafka producer using the provided
	 * rapidjson::Document
	 *
	 * \param configJSON - A reference to a rapidjson::Value containing the
	 * producer configuration.
	 * \param topicConfigJSON - A reference to a rapidjson::Value containing the
	 * topic configuration.
	 */
	Producer(rapidjson::Value &configJSON, rapidjson::Value &topicConfigJSON);

	/**
	 * \brief Producer advanced constructor
	 *
	 * The advanced constructor for the Producer class.
	 * Initializes members to default values.
	 * Sets up the class and rdkafka producer using the provided
	 * json formatted string
	 *
	 * \param configString - A json formatted std::string containing the
	 * producer configuration.
	 * \param topicConfigString - A json formatted std::string containing the
	 * topic configuration.
	 */
	Producer(std::string configString, std::string topicConfigString);

	/**
	 * \brief Producer destructor
	 *
	 * The destructor for the Producer class.
	 */
	~Producer();

	/**
	 * \brief Producer setup function
	 *
	 * Sets up the class and creates the rdkafka producer using the provided
	 * rapidjson::Document
	 *
	 * \param configJSON - A reference to a rapidjson::Value containing the
	 * producer configuration.
	 * \param topicConfigJSON - A reference to a rapidjson::Value containing the
	 * topic configuration.
	 */
	virtual void setup(rapidjson::Value &configJSON,
			rapidjson::Value &topicConfigJSON) override;

	/**
	 * \brief Producer setup function
	 *
	 * Sets up the class and creates the rdkafka producer using the provided
	 * json formatted string
	 *
	 * \param configString - A json formatted std::string containing the
	 * producer configuration.
	 * \param topicConfigString - A json formatted std::string containing the
	 * topic configuration.
	 */
	virtual void setup(std::string configString, std::string topicConfigString)
			override;

	/**
	 * \brief Producer topic creation function
	 *
	 * Creates a RdKafka::Topic based on the provided string and optional
	 * configuration.
	 *
	 * \param topic - A std::string containing name of the topic.
	 * \param tconf - An optional RdKafka::Conf * object containing the topic
	 * specific configuration.
	 * \return Returns a pointer to the created RdKafka::Topic if successful,
	 * NULL otherwise.
	 */
	RdKafka::Topic * createTopic(std::string topic,
			RdKafka::Conf *tconf = NULL);

	/**
	 * \brief Producer binary send function
	 *
	 * Sends the binary (byte) data of the provided length to the provided topic.
	 *
	 * \param topic - A pointer to the RdKafka::Topic idenfifying the kafka topic
	 * \param data - A pointer to the array of byte data to send.
	 * \param dataLength - The size_t of the byte array.
	 */
	void send(RdKafka::Topic *topic, byte* data, size_t dataLength);

	/**
	 * \brief Producer string send function
	 *
	 * Sends the string message to the provided topic.
	 *
	 * \param topic - A pointer to the RdKafka::Topic idenfifying the kafka topic
	 * \param message - The std::string message to send.
	 */
	void sendString(RdKafka::Topic *topic, std::string message);

	/**
	 * \brief Producer polling function
	 *
	 * Polls kafka to allow various internal processes to occur if there are no
	 * messages
	 *
	 * \param timeout - A long containing the time to wait while polling
	 */
	void poll(long timeout);

private:

	/**
	 * \brief rdkafka producer pointer
	 *
	 * The RdKafka::Producer * variable
	 */
	RdKafka::Producer * m_pProducer;
};

}
#endif
