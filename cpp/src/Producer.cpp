#include "Producer.h"
#include "rdkafkacpp.h"

// Define configuration type
#define CONFIGTYPE_STRING "ProducerConfig"

namespace hazdevbroker {

Producer::Producer() {

	// init
	m_pProducer = NULL;
	m_sConfigType = std::string(CONFIGTYPE_STRING);
}

Producer::Producer(rapidjson::Value &configJSON,
		rapidjson::Value &topicConfigJSON) {

	// init
	m_pProducer = NULL;
	m_sConfigType = std::string(CONFIGTYPE_STRING);

	// setup using json
	setup(configJSON, topicConfigJSON);

}

Producer::Producer(std::string configString, std::string topicConfigString) {

	// init
	m_pProducer = NULL;
	m_sConfigType = std::string(CONFIGTYPE_STRING);

	// setup using a string
	setup(configString, topicConfigString);

}

Producer::~Producer() {

	// cleanup
	if (m_pProducer != NULL) {
		delete m_pProducer;
	}
}

void Producer::setup(rapidjson::Value &configJSON,
		rapidjson::Value &topicConfigJSON) {
	std::string errstr;

	// create config
	RdKafka::Conf *conf = convertJSONConfigToProp(configJSON, topicConfigJSON);

	// config created?
	if (conf == NULL) {
		m_pProducer = NULL;
		return;
	}

	// create producer
	m_pProducer = RdKafka::Producer::create(conf, errstr);

	// error check
	if (!m_pProducer) {
		log("Error Creating m_pProducer: " + errstr);
		m_pProducer = NULL;
	}

	// cleanup
	delete conf;
}

void Producer::setup(std::string configString, std::string topicConfigString) {

	std::string errstr;

	// create config
	RdKafka::Conf *conf = convertJSONStringToProp(configString,
			topicConfigString);

	// config created?
	if (conf == NULL) {
		m_pProducer = NULL;
		return;
	}

	// create producer
	m_pProducer = RdKafka::Producer::create(conf, errstr);

	// error check
	if (!m_pProducer) {
		log("Error Creating m_pProducer: " + errstr);
		m_pProducer = NULL;
	}

	// cleanup
	delete conf;
}

RdKafka::Topic * Producer::createTopic(std::string topic,
		RdKafka::Conf *tconf) {

	// nullcheck
	if (m_pProducer == NULL) {
		return (NULL);
	}

	std::string errstr;
	RdKafka::Topic *newtopic = NULL;

	// create the topic
	newtopic = RdKafka::Topic::create(m_pProducer, topic, tconf, errstr);

	// error check
	if (!newtopic) {
		log("Error creating topic: " + errstr);
		return (NULL);
	}

	return (newtopic);
}

void Producer::send(RdKafka::Topic *topic, byte* data, size_t dataLength) {

	// nullcheck
	if (m_pProducer == NULL) {
		return;
	}

	// send data to kafka
	RdKafka::ErrorCode resp = m_pProducer->produce(topic,
			RdKafka::Topic::PARTITION_UA, RdKafka::Producer::RK_MSG_COPY, data,
			dataLength, NULL, NULL);

	// were there any errors
	if (resp != RdKafka::ERR_NO_ERROR) {
		log(
				"Error producing data to topic: "
						+ std::string(RdKafka::err2str(resp)));
	}

	// let kafka do its internal stuff
	poll(0);
}

void Producer::sendString(RdKafka::Topic *topic, std::string message) {

	// nullcheck
	if (m_pProducer == NULL) {
		return;
	}

	// convert message to binary data
	byte * data = new byte[message.length()];
	std::copy(message.begin(), message.end(), data);

	// send the string as binary data
	send(topic, data, message.size());

	// cleanup
	if (data != NULL)
		delete (data);
}

void Producer::poll(long timeout) {

	// nullcheck
	if (m_pProducer == NULL) {
		return;
	}

	// let kafka do its internal stuff
	m_pProducer->poll(timeout);
}

}
