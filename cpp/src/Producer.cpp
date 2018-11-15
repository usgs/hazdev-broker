#include <Producer.h>
#include <rdkafkacpp.h>
#include <Heartbeat.h>

#include <string>
#include <limits>
#include <cmath>
#include <ctime>
#include <algorithm>

// Define configuration type
#define CONFIGTYPE_STRING "ProducerConfig"

namespace hazdevbroker {

Producer::Producer() {
	// init
	m_pProducer = NULL;
	m_sConfigType = std::string(CONFIGTYPE_STRING);
	m_iHeartbeatInterval = -1;
	m_lLastHeartbeatTime = std::time(NULL);
}

Producer::Producer(rapidjson::Value &configJSON,
		rapidjson::Value &topicConfigJSON) {
	// init
	m_pProducer = NULL;
	m_sConfigType = std::string(CONFIGTYPE_STRING);
	m_iHeartbeatInterval = -1;
	m_lLastHeartbeatTime = std::time(NULL);

	// setup using json
	setup(configJSON, topicConfigJSON);
}

Producer::Producer(std::string configString, std::string topicConfigString) {
	// init
	m_pProducer = NULL;
	m_sConfigType = std::string(CONFIGTYPE_STRING);
	m_iHeartbeatInterval = -1;
	m_lLastHeartbeatTime = std::time(NULL);

	// setup using a string
	setup(configString, topicConfigString);
}

Producer::Producer(rapidjson::Value &configJSON,
		rapidjson::Value &topicConfigJSON, int hbInterval) {
	// init
	m_pProducer = NULL;
	m_sConfigType = std::string(CONFIGTYPE_STRING);
	m_iHeartbeatInterval = hbInterval;
	m_lLastHeartbeatTime = std::time(NULL);

	// setup using json
	setup(configJSON, topicConfigJSON);
}

Producer::Producer(std::string configString, std::string topicConfigString,
		int hbInterval) {
	// init
	m_pProducer = NULL;
	m_sConfigType = std::string(CONFIGTYPE_STRING);
	m_iHeartbeatInterval = hbInterval;
	m_lLastHeartbeatTime = std::time(NULL);

	// setup using a string
	setup(configString, topicConfigString);
}

Producer::~Producer() {
	// cleanup
	if (m_pProducer != NULL) {
		delete (m_pProducer);
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
		RdKafka::Conf *topicConfig) {
	// nullcheck
	if (m_pProducer == NULL) {
		return (NULL);
	}

	std::string errstr;
	RdKafka::Topic *newtopic = NULL;

	// create the topic
	newtopic = RdKafka::Topic::create(m_pProducer, topic, topicConfig, errstr);

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
		log("Error producing data to topic: "
			+ std::string(RdKafka::err2str(resp)));
	}

	// send heartbeat message, will not send if heartbeats
	// are disabled, or if it has not been long enough to
	// send a heartbeat
	sendHeartbeat(topic);

	// let kafka do its internal stuff
	poll(0);
}

void Producer::sendHeartbeat(RdKafka::Topic *topic) {
	// don't send heartbeat if it's disabled
	if (m_iHeartbeatInterval < 0) {
		return;
	}

	// get current time in seconds
	int64_t timeNow = std::time(NULL);

	// calculate elapsed time
	int64_t elapsedTime = timeNow - m_lLastHeartbeatTime;

	// has it been long enough since the last heartbeat?
	// or are we always sending heartbeats?
	if ((elapsedTime >= m_iHeartbeatInterval) ||
			(m_iHeartbeatInterval == 0)) {
		// create the heartbeat
		Heartbeat newHeartbeat(timeNow, topic->name(), m_sClientId);

		// send the heartbeat
		if (newHeartbeat.isValid()) {
			std::string heartbeatString = newHeartbeat.toJSONString();

			byte * heartbeatData = new byte[heartbeatString.length()];
			std::copy(heartbeatString.begin(), heartbeatString.end(),
				heartbeatData);

			// send heartbeat to kafka
			RdKafka::ErrorCode resp = m_pProducer->produce(topic,
				RdKafka::Topic::PARTITION_UA, RdKafka::Producer::RK_MSG_COPY,
				heartbeatData, heartbeatString.size(), NULL, NULL);

			// were there any errors
			if (resp != RdKafka::ERR_NO_ERROR) {
				log("Error producing heartbeat to topic: "
					+ std::string(RdKafka::err2str(resp)));
			}

			// let kafka do its internal stuff
			poll(0);
		}

		// remember heartbeat time
		m_lLastHeartbeatTime = timeNow;
	}
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
	if (data != NULL) {
		delete[] (data);
	}
}

void Producer::poll(int64_t timeout) {
	// nullcheck
	if (m_pProducer == NULL) {
		return;
	}

	// let kafka do its internal stuff
	m_pProducer->poll(timeout);
}

int Producer::getHeartbeatInterval() {
	return(m_iHeartbeatInterval);
}

void Producer::setHeartbeatInterval(int heartbeatInterval) {
	m_iHeartbeatInterval = heartbeatInterval;
}
}  // namespace hazdevbroker
