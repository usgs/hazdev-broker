#include <Consumer.h>
#include <rdkafkacpp.h>
#include <limits>
#include <cmath>
#include <algorithm>

// Define configuration type
#define CONFIGTYPE_STRING "ConsumerConfig"

namespace hazdevbroker {

Consumer::Consumer() {
	// init
	m_pConsumer = NULL;
	m_sConfigType = std::string(CONFIGTYPE_STRING);
	m_TopicList.clear();
	m_sHeartbeatDirectory = "";
	m_lLastHeartbeatTime = std::time(NULL);
}

Consumer::Consumer(rapidjson::Value &configJSON, // NOLINT
		rapidjson::Value &topicConfigJSON) { // NOLINT
	// init
	m_pConsumer = NULL;
	m_sConfigType = std::string(CONFIGTYPE_STRING);
	m_TopicList.clear();
	m_sHeartbeatDirectory = "";
	m_lLastHeartbeatTime = std::time(NULL);

	// setup using json
	setup(configJSON, topicConfigJSON);
}

Consumer::Consumer(std::string configString, std::string topicConfigString) {

	// init
	m_pConsumer = NULL;
	m_sConfigType = std::string(CONFIGTYPE_STRING);
	m_TopicList.clear();
	m_sHeartbeatDirectory = "";
	m_lLastHeartbeatTime = std::time(NULL);

	// setup using a string
	setup(configString, topicConfigString);
}

Consumer::Consumer(rapidjson::Value &configJSON, // NOLINT
		rapidjson::Value &topicConfigJSON, std::string hbDirectory) { // NOLINT
	// init
	m_pConsumer = NULL;
	m_sConfigType = std::string(CONFIGTYPE_STRING);
	m_TopicList.clear();
	m_sHeartbeatDirectory = hbDirectory;
	m_lLastHeartbeatTime = std::time(NULL);

	// setup using json
	setup(configJSON, topicConfigJSON);
}

Consumer::Consumer(std::string configString, std::string topicConfigString,
		std::string hbDirectory) {

	// init
	m_pConsumer = NULL;
	m_sConfigType = std::string(CONFIGTYPE_STRING);
	m_TopicList.clear();
	m_sHeartbeatDirectory = hbDirectory;
	m_lLastHeartbeatTime = std::time(NULL);

	// setup using a string
	setup(configString, topicConfigString);
}

Consumer::~Consumer() {
	// cleanup
	if (m_pConsumer != NULL) {
		m_pConsumer->close();
		delete (m_pConsumer);
	}
}

void Consumer::setup(rapidjson::Value &configJSON,
		rapidjson::Value &topicConfigJSON) {
	std::string errstr;

	// create config
	RdKafka::Conf *conf = convertJSONConfigToProp(configJSON, topicConfigJSON);

	// config created?
	if (conf == NULL) {
		m_pConsumer = NULL;
		return;
	}

	// create consumer
	m_pConsumer = RdKafka::KafkaConsumer::create(conf, errstr);

	// error check
	if (!m_pConsumer) {
		log("Error Creating consumer: " + errstr);
		m_pConsumer = NULL;
	}

	// cleanup
	delete conf;
}

void Consumer::setup(std::string configString, std::string topicConfigString) {
	std::string errstr;

	// create config
	RdKafka::Conf *conf = convertJSONStringToProp(configString,
			topicConfigString);

	// config created?
	if (conf == NULL) {
		m_pConsumer = NULL;
		return;
	}

	// create consumer
	m_pConsumer = RdKafka::KafkaConsumer::create(conf, errstr);

	// error check
	if (!m_pConsumer) {
		log("Error Creating consumer: " + errstr);
		m_pConsumer = NULL;
	}

	// cleanup
	delete conf;
}

bool Consumer::subscribe(std::string topic) {
	// nullcheck
	if (m_pConsumer == NULL) {
		return (false);
	}

	// convert topic string to vector
	std::vector <std::string> topics;
	topics.push_back(topic);

	// subscribe
	return (subscribe(topics));
}

bool Consumer::subscribe(std::vector<std::string> topics) {
	// nullcheck
	if (m_pConsumer == NULL) {
		return (false);
	}

	// subscribe to the topics
	RdKafka::ErrorCode err = m_pConsumer->subscribe(topics);

	// error check
	if (err) {
		log("Error subscribing to topic(s): " + RdKafka::err2str(err));
		return (false);
	}

	// remember the topic list for handling heartbeats
	for (auto topic : topics) {
		m_TopicList.push_back(topic);
	}

	return (true);
}

byte * Consumer::poll(int64_t timeout, size_t *datalength) {
	byte * data = NULL;

	// nullcheck
	if (m_pConsumer == NULL) {
		return (NULL);
	}

	// Negative value means wait for an arbitrarily long time
	if (timeout < 0) {
		timeout = std::numeric_limits<int64_t>::max();
	}

	// get any messages pending for our topic(s) from kafka
	RdKafka::Message *msg = m_pConsumer->consume(timeout);

	// we get something?
	if (msg->err() == RdKafka::ERR_NO_ERROR) {
		// get length
		*datalength = msg->len();

		// copy data
		data = new byte[*datalength];
		memcpy(data, static_cast<byte *>(msg->payload()), *datalength);

	} else if (msg->err() == RdKafka::ERR__TIMED_OUT) {
		// cleanup
		delete (msg);

		// got nothing
		*datalength = 0;
		return (NULL);
	} else if (msg->err() == RdKafka::ERR__PARTITION_EOF) {
		// cleanup
		delete (msg);

		// got nothing
		*datalength = 0;
		return (NULL);
	} else {
		// error
		log("Error polling broker cluster " + RdKafka::err2str(msg->err())
			+ ": " + std::string(msg->errstr()));

		// cleanup
		delete (msg);

		// got nothing
		*datalength = 0;
		return (NULL);
	}

	// cleanup
	delete (msg);

	// handle  heartbeats
	if ((data != NULL) && (*datalength != 0)) {
		// convert to string
		std::string stringData = "";
		try {
			// convert bytes to string
			stringData = std::string(reinterpret_cast<const char *>(data),
					*datalength);
		} catch (const std::exception &e) {
			// we can safely ignore this, if it can't convert, it's
			// definitely not a heartbeat
			return (data);
		}

		// check if this is a heartbeat
		if (m_HeartbeatProcessor.fromJSONString(stringData) == true) {
			// handle it
			handleHeartbeat(m_HeartbeatProcessor);

			// cleanup heartbeat
			delete (data);

			// we don't return anything for heartbeats
			*datalength = 0;
			return (NULL);
		} else {
			// done
			return (data);
		}
	}

	// done
	return (data);
}

std::string Consumer::pollString(int64_t timeout) {
	size_t length = 0;
	byte* byteMessage = NULL;

	// nullcheck
	if (m_pConsumer == NULL) {
		return ("");
	}

	// get data
	byteMessage = poll(timeout, &length);

	// did we get anything?
	if ((byteMessage == NULL) || (length == 0)) {
		return ("");
	}

	std::string stringMessage = "";
	try {
		// convert bytes to string
		stringMessage = std::string(reinterpret_cast<const char *>(byteMessage),
				length);
	} catch (const std::exception &e) {
		log("Exception converting bytes to string: " + std::string(e.what()));
		return ("");
	}

	// cleanup
	delete (byteMessage);

	// done
	return (stringMessage);
}

void Consumer::handleHeartbeat(hazdevbroker::Heartbeat aHeartbeat) {
	// is this a valid heartbeat
	if (aHeartbeat.isValid() == false) {
		return;
	}

	// is this heartbeat for one of the configured topics
	// we don't want to handle hearbeats for other topics
	if(std::find(m_TopicList.begin(), m_TopicList.end(), aHeartbeat.topic)
			== m_TopicList.end()) {
		return;
	}

	// set the time the heartbeat was recieved in case our
	// caller is monitoring this
	setLastHeartbeatTime(std::time(NULL));

	// write the heartbeat to disk (won't write if heartbeatDirectory is
	// empty)
	aHeartbeat.writeToDisk(m_sHeartbeatDirectory);
}

int64_t Consumer::getLastHeartbeatTime() {
	return(m_lLastHeartbeatTime);
}

void Consumer::setLastHeartbeatTime(int64_t lastHeartbeatTime) {
	m_lLastHeartbeatTime = lastHeartbeatTime;
}

std::string Consumer::getHeartbeatDirectory() {
	return(m_sHeartbeatDirectory);
}

void Consumer::setHeartbeatDirectory(std::string heartbeatDirectory) {
	m_sHeartbeatDirectory = heartbeatDirectory;
}
}  // namespace hazdevbroker
