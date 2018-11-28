/*****************************************
 * This file is documented for Doxygen.
 * If you modify this file please update
 * the comments so that Doxygen will still
 * be able to work.
 ****************************************/
#ifndef HAZDEVBROKER_UTILITY_H
#define HAZDEVBROKER_UTILITY_H
#include <exception>
#include <string>

// needed to disable rapidjson warnings for clang
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wexpansion-to-defined"
#include "document.h" // NOLINT
#include "writer.h" // NOLINT
#include "stringbuffer.h"  // NOLINT
#pragma clang diagnostic pop

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
	 * \return Returns a reference to the populated rapidjson::Document
	 */
	rapidjson::Document & fromJSONString(std::string jsonstring,
										 rapidjson::Document & jsondocument); // NOLINT

	/**
	 * \brief Strip comments from a line
	 *
	 * Strips the provided string of comment blocks identified by the given 
	 * comment identifier. This function will return any characters up to the
	 * comment identifier and not return any characters after (and including) the
	 * comment identifier up to the end of provided line string. This function 
	 * will return  an empty string if the line string starts with a comment 
	 * identifier.
	 * 
	 * \param line - A std::string containing the line to strip
     * \param commentIdentifier - A std::string containing the comment 
	 * identifier character/string
	 * \return Returns a std::string containing the stripped string
	 */
	std::string stripCommentsFromLine(std::string line,
									  std::string commentIdentifier);

}  // namespace hazdevbroker
#endif  // HAZDEVBROKER_UTILITY_H
