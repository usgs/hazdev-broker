#include <Heartbeat.h>
#include <Utility.h>

#include <limits>
#include <vector>
#include <string>
#include <cmath>
#include <sstream>
#include <fstream>
#include <thread>
#include <chrono>

namespace hazdevbroker {

// JSON Keys
#define TYPE_KEY "Type"
#define HEARTBEAT_TYPE "Heartbeat"
#define TIME_KEY "Time"
#define TOPIC_KEY "Topic"
#define CLIENTID_KEY "ClientId"

Heartbeat::Heartbeat() {
    m_dTime = std::numeric_limits<double>::quiet_NaN();
    m_sTopic = "";
    m_sClientId = "";
}

Heartbeat::Heartbeat(double newTime, std::string newTopic,
        std::string newClientId) {
    m_dTime = newTime;
    m_sTopic = newTopic;
    m_sClientId = newClientId;
}

Heartbeat::Heartbeat(const Heartbeat & newHeartbeat) {
    m_dTime = newHeartbeat.m_dTime;
    m_sTopic = newHeartbeat.m_sTopic;
    m_sClientId = newHeartbeat.m_sClientId;
}

Heartbeat::~Heartbeat() {
}

std::string Heartbeat::toJSONString() {
    rapidjson::Document json;
    json.SetObject();

    // type
    rapidjson::Value typeValue;
    typeValue.SetString(rapidjson::StringRef(HEARTBEAT_TYPE),
        json.GetAllocator());
    json.AddMember(TYPE_KEY, typeValue, json.GetAllocator());

    // m_dTime
	if (std::isnan(m_dTime) != true) {
		std::string timeString = convertEpochTimeToISO8601(m_dTime);
		rapidjson::Value timeValue;
		timeValue.SetString(rapidjson::StringRef(timeString.c_str()),
							json.GetAllocator());
		json.AddMember(TIME_KEY, timeValue, json.GetAllocator());
	}

	// m_sTopic
	if (m_sTopic != "") {
		rapidjson::Value topicValue;
		topicValue.SetString(rapidjson::StringRef(m_sTopic.c_str()),
			json.GetAllocator());
		json.AddMember(TOPIC_KEY, topicValue, json.GetAllocator());
	}

	// m_sClientId
	if (m_sClientId != "") {
		rapidjson::Value clientIdValue;
		clientIdValue.SetString(rapidjson::StringRef(m_sClientId.c_str()),
			json.GetAllocator());
		json.AddMember(CLIENTID_KEY, clientIdValue, json.GetAllocator());
	}

    return(hazdevbroker::toJSONString(json));
}

bool Heartbeat::fromJSONString(std::string jsonString) {
    try {
        // convert from json
        rapidjson::Document jsonDocument;
        rapidjson::Value &json = hazdevbroker::fromJSONString(jsonString,
            jsonDocument);

        // type
        if ((json.HasMember(TYPE_KEY) == true)
                && (json[TYPE_KEY].IsString() == true)) {
            std::string type = std::string(json[TYPE_KEY].GetString(),
                                json[TYPE_KEY].GetStringLength());

            if (type != HEARTBEAT_TYPE) {
                return(false);
            }
        } else {
            return(false);
        }

        // time
        if ((json.HasMember(TIME_KEY) == true)
                && (json[TIME_KEY].IsString() == true)) {
            m_dTime = hazdevbroker::convertISO8601ToEpochTime(
                    std::string(json[TIME_KEY].GetString(),
                                json[TIME_KEY].GetStringLength()));
        } else {
            m_dTime = std::numeric_limits<double>::quiet_NaN();
        }

        // topic
        if ((json.HasMember(TOPIC_KEY) == true) &&
            (json[TOPIC_KEY].IsString() == true)) {
            m_sTopic = std::string(json[TOPIC_KEY].GetString(),
                                json[TOPIC_KEY].GetStringLength());
        } else {
            m_sTopic = "";
        }

        // clientId
        if ((json.HasMember(CLIENTID_KEY) == true) &&
            (json[CLIENTID_KEY].IsString() == true)) {
            m_sClientId = std::string(json[CLIENTID_KEY].GetString(),
                                json[CLIENTID_KEY].GetStringLength());
        } else {
            m_sClientId = "";
        }
    } catch (const std::exception&) {
        return(false);
    }

    return(true);
}

void Heartbeat::writeToDisk(std::string heartbeatDirectory) {
    if (heartbeatDirectory == "") {
        return;
    }
    if (isValid() == false) {
        return;
    }

     // build heartbeat filename from the m_sTopic name and client id
    std::string heartbeatFileName = heartbeatDirectory + "/" +
        m_sTopic + "_" + m_sClientId + ".heartbeat";

    // create file
	std::ofstream hbFile;
	hbFile.open(heartbeatFileName, std::ios::out);
	if ((hbFile.rdstate() & std::ifstream::failbit) != 0) {
		// sleep a little while
		std::this_thread::sleep_for(std::chrono::milliseconds(100));

		// try again
		hbFile.open(heartbeatFileName, std::ios::out);
		if ((hbFile.rdstate() & std::ifstream::failbit) != 0) {
			return;
		}
	}

	// write file
	try {
		hbFile << toJSONString();
	} catch (const std::exception &e) {
		return;
	}

	// done
	hbFile.close();
}

bool Heartbeat::isValid() {
	std::vector<std::string> errorlist = getErrors();
	std::string errorstring = "";

	if (errorlist.size() == 0) {
		// no errors
		return (true);
	} else {
		return (false);
	}
}

std::vector<std::string> Heartbeat::getErrors() {
    std::vector<std::string> errorList;

    // Required Keys
    // time
	if (std::isnan(m_dTime) == true) {
		errorList.push_back("No Time in Heartbeat Class.");
	}

	// topic
	if (m_sTopic == "") {
		// empty m_sTopic
		errorList.push_back("Empty Topic in Heartbeat Class.");
	}

	// clientId
	if (m_sClientId == "") {
		// empty m_sClientId
		errorList.push_back("Empty Client Id in Heartbeat Class.");
	}

    return(errorList);
}
}  // namespace hazdevbroker
