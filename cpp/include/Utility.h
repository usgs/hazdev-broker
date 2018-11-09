/*****************************************
 * This file is documented for Doxygen.
 * If you modify this file please update
 * the comments so that Doxygen will still
 * be able to work.
 ****************************************/
#ifndef HAZDEVBROKER_UTILITY_H
#define HAZDEVBROKER_UTILITY_H

// needed to disable rapidjson warnings for clang
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wexpansion-to-defined"
#include <document.h>
#include <writer.h>
#include <stringbuffer.h>
#pragma clang diagnostic pop

#include <exception>
#include <string>

/**
 * \typedef byte
 *
 * \brief byte format for hazdevbroker
 *
 * The hazdevbroker byte type is a typedef to set std::uint8_t as byte for
 * code readabilty and to match the similar java implementation.
 */
#ifndef byte
typedef std::uint8_t byte;
#endif

/**
 * \namespace hazdevbroker
 * \brief hazdevbroker namespace for hazdevbroker client classes and functions
 *
 * The hazdevbroker namespace contains various classes and functions used
 * to create producer and consumer clients used to connect to the hazdevbroker.
 */
namespace hazdevbroker {
	/**
	 * \brief Convert iso8601 time string to decimal epoch seconds
	 *
	 * Converts the provided iso8601 string to decimal epoch seconds
	 * \return Returns a double containing the decimal epoch seconds
	 */
	double convertISO8601ToEpochTime(std::string TimeString);

	/**
	 * \brief Convert decimal epoch seconds to iso8601 time string
	 *
	 * Converts the  decimal epoch seconds to iso8601 time string
	 * \return Returns a std::string containing iso8601 time string
	 */
	std::string convertEpochTimeToISO8601(double epochtime);

	/**
	 * \brief Convert to json string function
	 *
	 * Converts the contents of the class to a serialized json string
	 * \return Returns a std::string containing the serialized json string
	 */
	std::string toJSONString(rapidjson::Value &json); // NOLINT

	/**
	 * \brief Convert from json string function
	 *
	 * Converts the provided string from a serialized json string, populating
	 * members
	 * \param jsonstring - A std::string containing the serialized json
     * \param jsondocument - A reference to the rapidjson::Document to populate
	 * \return Returns A reference to the populated rapidjson::Document
	 */
	rapidjson::Document & fromJSONString(std::string jsonstring,
										 rapidjson::Document & jsondocument); // NOLINT

}  // namespace hazdevbroker
#endif  // HAZDEVBROKER_UTILITY_H
