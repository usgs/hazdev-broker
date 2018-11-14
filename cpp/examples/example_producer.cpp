#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
#include <cstdlib>
#include <cstdio>

#include "Producer.h"

// json keys
#define BROKER_CONFIG "HazdevBrokerConfig"
#define TOPIC_CONFIG "HazdevBrokerTopicConfig"
#define TOPIC "Topic"

// example logging function
void logProducer(const std::string &message) {
	std::cerr << message << std::endl;
}

int main(int argc, char **argv) {
	if (argc != 2) {
		std::cerr << "Usage: " << argv[0] << " <configfile>" << std::endl;
		exit(1);
	}

	// read config file
	std::ifstream configFile;
	configFile.open(argv[1]);

	if (!configFile.good()) {
		std::cerr << "Unable to open configuration file: " << argv[1]
				<< std::endl;
		exit(1);
	}

	std::stringstream configBuffer;
	configBuffer << configFile.rdbuf();
	std::string configString = configBuffer.str();
	configFile.close();

	// parse config file into json
	rapidjson::Document configJSON;
	if (configJSON.Parse(configString.c_str()).HasParseError()) {
		std::cerr << "Error Parsing config string to JSON." << std::endl;
		exit(1);
	}

	// get broker config
	rapidjson::Value brokerConfig;
	if ((configJSON.HasMember(BROKER_CONFIG) == true)
			&& (configJSON[BROKER_CONFIG].IsObject() == true)) {
		brokerConfig = configJSON[BROKER_CONFIG];
	} else {
		std::cerr << "Error, did not find " << std::string(BROKER_CONFIG)
				<< "in configuration." << std::endl;
		exit(1);
	}

	// get topic config
	rapidjson::Value topicConfig;
	if ((configJSON.HasMember(TOPIC_CONFIG) == true)
			&& (configJSON[TOPIC_CONFIG].IsObject() == true)) {
		topicConfig = configJSON[TOPIC_CONFIG];
	}

	// get producer topic
	std::string producerTopic;
	if ((configJSON.HasMember(TOPIC) == true)
			&& (configJSON[TOPIC].IsString() == true)) {
		producerTopic = configJSON[TOPIC].GetString();
	} else {
		std::cerr << "Error, did not find " << std::string(TOPIC)
				<< "in configuration." << std::endl;
		exit(1);
	}

	// create producer
	hazdevbroker::Producer * m_Producer = new hazdevbroker::Producer();
	// set up logging
	m_Producer->setLogCallback(std::bind(&logProducer, std::placeholders::_1));
	// set up producer
	m_Producer->setup(brokerConfig, topicConfig);
	// set heartbeat interval to -1 so that the producer always sends
	// heartbeats
	m_Producer->setHeartbeatInterval(-1);

	// create topic handle
	RdKafka::Topic * m_ProducerTopic = m_Producer->createTopic(producerTopic);

	// run until stopped
	std::string consoleMessage = "";
	std::cout << "Type <quit> to stop sending messages." << std::endl;
	while (true) {
		// get message from input
		std::getline(std::cin, consoleMessage);

		if (consoleMessage == "quit") {
			// done sending messages
			break;
		} else {
			// send message
			m_Producer->sendString(m_ProducerTopic, consoleMessage);
		}
	}

	delete (m_Producer);
	delete (m_ProducerTopic);
	exit(0);
}
