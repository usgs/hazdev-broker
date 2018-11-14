/*****************************************
 * This file is documented for Doxygen.
 * If you modify this file please update
 * the comments so that Doxygen will still
 * be able to work.
 ****************************************/
#ifndef HAZDEVBROKER_HEARTBEAT_H
#define HAZDEVBROKER_HEARTBEAT_H

#include <Utility.h>

#include <string>
#include <vector>
#include <exception>
#include <functional>
#include <iostream>
#include <cstdlib>
#include <cstdio>
#include <csignal>
#include <cstring>

namespace hazdevbroker {

/**
 * \brief hazdevbroker Heartbeat conversion class
 *
 * The hazdevbroker Heartbeat class is a conversion class used to generate, 
 * parse, and check heartbeat messages.
 *
 */
class Heartbeat  {
 public:
	/**
	 * \brief Heartbeat constructor
	 *
	 * The constructor for the Heartbeat class.
	 * Initializes members to null values.
	 */
	Heartbeat();

	/**
	 * \brief Heartbeat advanced constructor
	 *
	 * The advanced constructor for the source class.
	 * Initializes members to provided values.
	 *
	 * \param newTime - A std::string containing the time to use
	 * \param newTopic - A std::string containing the topic for this heartbeat
	 * \param newClientId - A std::string containing the client id for this 
     *  heartbeat
	 */
	Heartbeat(double newTime, std::string newTopic, std::string newClientId);

	/**
	 * \brief Heartbeat copy constructor
	 *
	 * The copy constructor for the Heartbeat class.
	 * Copies the provided object from a Heartbeat, populating members
	 * \param newHeartbeat - A Heartbeat.
	 */
	Heartbeat(const Heartbeat & newHeartbeat);

	/**
	 * \brief Heartbeat destructor
	 *
	 * The destructor for the Heartbeat class.
	 */
	~Heartbeat();

	/**
	 * \brief Heartbeat json string converter
	 *
	 * Converts the contents of the class to a json string
	 * \returns Returns a std::string containing the class contents formatted in 
     * json
	 */
    std::string toJSONString();

	/**
	 * \brief Heartbeat json string inputter
	 *
	 * Converts the provided serialized JSON string, into the class, populating
	 * members
     * \param jsonString - A std::string containing the json string
	 * \returns true if successful, false otherwise
	 */
    bool fromJSONString(std::string jsonString);

    /**
	 * \brief Write Heartbeat to disk
	 *
	 * Writes the heartbeat to a disk file
     * \param heartbeatDirectory - A std::string containing location to write the
     * heartbeat file
	 */
    void writeToDisk(std::string heartbeatDirectory);

	/**
	 * \brief Validates the values in the class
	 *
	 * Validates the values contained in the class
	 * \return Returns 1 if successful, 0 otherwise
	 */
    bool isValid();

	/**
	 * \brief Gets any errors in the class
	 *
	 * Gets any formatting errors in the class
	 * \return Returns a std::vector<std::string> containing the errors
	 */
	std::vector<std::string> getErrors();

	/**
	 * \brief Heartbeat time
	 *
	 * A required double containing the time for this heartbeat in epoch time
     * (seconds since 1970)
	 */
	double m_dTime;

	/**
	 * \brief Heartbeat topic
	 *
	 * An optional std::string containing the topic for this Heartbeat.
	 */
	std::string m_sTopic;

	/**
	 * \brief Heartbeat clientId code
	 *
	 * A required std::string containing the clientId for this Heartbeat.
	 */
	std::string m_sClientId;
};
}  // namespace hazdevbroker
#endif  // HAZDEVBROKER_HEARTBEAT_H
