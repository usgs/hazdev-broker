#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
#include <cstdlib>
#include <cstdio>
#include <ctime>

#include "Consumer.h"

// json keys
#define BROKER_CONFIG "HazdevBrokerConfig"
#define TOPIC_CONFIG "HazdevBrokerTopicConfig"
#define TOPIC_LIST "TopicList"

// example logging function
void logConsumer(std::string message) {
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

	// this section is responsible for pre-parsing the config file and removing
	// any comment lines uses hazdevbroker::stripCommentsFromLine() to do the
	// actual stripping.
	std::string line;
	std::string configString = "";
	while (std::getline(configFile, line)) {
		if (line.empty()) {
        	continue;
		}

		configString += hazdevbroker::stripCommentsFromLine(line, "#");
	}
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

	// get topic list
	std::vector < std::string > topicList;
	if ((configJSON.HasMember(TOPIC_LIST) == true)
			&& (configJSON[TOPIC_LIST].IsArray() == true)) {
		rapidjson::Value topicArray = configJSON[TOPIC_LIST].GetArray();
		// convert to string vector
		for (int i = 0; i < topicArray.Size(); i++) {
			topicList.push_back(std::string(topicArray[i].GetString()));
		}
	} else {
		std::cerr << "Error, did not find " << std::string(TOPIC_LIST)
				<< "in configuration." << std::endl;
		exit(1);
	}

	// create consumer
	hazdevbroker::Consumer * m_Consumer = new hazdevbroker::Consumer();
	// set up logging
	m_Consumer->setLogCallback(std::bind(&logConsumer, std::placeholders::_1));
	// set up consumer, set up default topic config
	m_Consumer->setup(brokerConfig, topicConfig);
	// set heartbeat directory
	m_Consumer->setHeartbeatDirectory("./");

	// subscribe to topics
	m_Consumer->subscribe(topicList);

	int64_t heartbeatInterval = 120;

	// run until stopped
	while (true) {
		// get message from broker
		std::string brokerMessage = m_Consumer->pollString(100);

		// print message
		if (brokerMessage != "") {
			std::cout << brokerMessage << std::endl;
		}

		// get current time in seconds
		int64_t timeNow = std::time(NULL);

		// get last heartbeat time
		int64_t lastHB = m_Consumer->getLastHeartbeatTime();

		// calculate elapsed time
		int64_t elapsedTime = timeNow - lastHB;

		// has it been too long since the last heartbeat?
		if (elapsedTime > heartbeatInterval) {
			std::cout << "No Heartbeat Message seen from topic(s)" <<
				" in " << std::to_string(heartbeatInterval) << " seconds! (" <<
				std::to_string(elapsedTime) << ")" << std::endl;

			// reset last heartbeat time so that we don't fill the
			// log
			m_Consumer->setLastHeartbeatTime(timeNow);
		}
	}

	delete (m_Consumer);
	exit(0);
}
