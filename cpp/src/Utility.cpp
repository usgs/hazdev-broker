#include <Utility.h>
#include <regex>
#include <string>

#if defined(_MSC_VER)
#define _CRT_SECURE_NO_WARNINGS
#endif

namespace hazdevbroker {

// Space where environment variable TZ will be
// stored after the first call to ConvertISO8601ToEpochTime()
#define MAXENV 128
char envTZ[MAXENV];
double convertISO8601ToEpochTime(std::string TimeString) {
	// make sure we got something
	if (TimeString.length() == 0) {
		return (-1.0);
	}

	// time string is too short
	if (TimeString.length() < 24) {
		return (-1.0);
	}

	// time string is too long
	if (TimeString.length() > 24) {
		return (-1.0);
	}

	struct tm timeinfo;
	memset(&timeinfo, 0, sizeof(struct tm));

	// Time string is in ISO8601 format:
	// 000000000011111111112222
	// 012345678901234567890123
	// YYYY-MM-DDTHH:MM:SS.SSSZ

	// year (0-3 in ISO8601 string)
	// struct tm stores year as "current year minus 1900"
	timeinfo.tm_year = atoi(TimeString.substr(0, 4).c_str()) - 1900;

	// month (5-6 in ISO8601 string)
	// struct tm stores month  as number from 0 to 11, January = 0
	timeinfo.tm_mon = atoi(TimeString.substr(5, 2).c_str()) - 1;

	// day (8-9 in ISO8601 string)
	timeinfo.tm_mday = atoi(TimeString.substr(8, 2).c_str());

	// hour (11-12 in ISO8601 string)
	timeinfo.tm_hour = atoi(TimeString.substr(11, 2).c_str());

	// minute (14-15 in ISO8601 string)
	timeinfo.tm_min = atoi(TimeString.substr(14, 2).c_str());

	// decimal seconds (17-22 in ISO8601 string)
	double seconds = atof(TimeString.substr(17, 6).c_str());

#ifdef _WIN32
	// windows specific timezone handling
	char * pTZ;
	char szTZExisting[32] = "TZ=";

	// Save current TZ setting locally
	pTZ = getenv("TZ");

	if(pTZ) {
		/* remember we have "TZ=" stored as the first 3 chars of the array */
		strncpy(&szTZExisting[3], pTZ, sizeof(szTZExisting) - 4);
		szTZExisting[sizeof(szTZExisting)-1] = 0x00;
	}

	// Change time zone to GMT
	_putenv("TZ=UTC");

	// set the timezone-offset to 0
	_timezone = 0;

	// set the DST-offset to 0
	_daylight = 0;

	// ensure _tzset() has been called, so that it isn't called again
	// automatically in mktime.
	_tzset();

	// convert to epoch time
	double usableTime = static_cast<double>(mktime(&timeinfo));

	// Restore original TZ setting
	_putenv(szTZExisting);
	_tzset();

#else
	// linux and other timezone handling
	char *tz;
	char TZorig[MAXENV];

	// Save current TZ setting locally
	tz = getenv("TZ");
	if (tz != reinterpret_cast<char *>(NULL)) {
		if (strlen(tz) > MAXENV - 4) {
			printf("ConvertISO8601ToEpochTime: unable to store current TZ "
					"environment variable.\n");
			return (-1);
		}
	}
	snprintf(TZorig, sizeof(TZorig), "TZ=%s", tz);

	// Change time zone to GMT
	snprintf(envTZ, sizeof(envTZ), "TZ=GMT");
	if (putenv(envTZ) != 0) {
		printf("ConvertISO8601ToEpochTime: putenv: unable to set TZ "
				"environment variable.\n");
		return (-1);
	}

	// convert to epoch time
	double usableTime = static_cast<double>(mktime(&timeinfo));

	// Restore original TZ setting
	snprintf(envTZ, sizeof(TZorig), "%s", TZorig);
	if (putenv(envTZ) != 0) {
		printf("ConvertISO8601ToEpochTime: putenv: unable to restore TZ "
				"environment variable.\n");
	}
	tzset();

#endif

	// add decimal seconds and return
	return (usableTime + seconds);
}

std::string convertEpochTimeToISO8601(double epochtime) {
	time_t time = static_cast<int>(epochtime);
	double decimalseconds = epochtime - static_cast<int>(time);

	// build the time portion, all but the seconds which are
	// seperate since time_t can't do decimal seconds
	char timebuf[sizeof "2011-10-08T07:07:"];
	tm* timestruct = gmtime(&time); // NOLINT
	strftime(timebuf, sizeof timebuf, "%Y-%m-%dT%H:%M:", timestruct);
	std::string timestring = timebuf;

	// build the seconds portion
	char secbuf[sizeof "00.000Z"];
	if ((timestruct->tm_sec + decimalseconds) < 10)
		snprintf(secbuf, sizeof(secbuf), "0%1.3f",
					timestruct->tm_sec + decimalseconds);
	else
		snprintf(secbuf, sizeof(secbuf), "%2.3f",
					timestruct->tm_sec + decimalseconds);
	std::string secondsstring = secbuf;

	// return the combined ISO8601 string
	return (timestring + secondsstring + "Z");
}

std::string toJSONString(rapidjson::Value &json) { // NOLINT
	// make the buffer
	rapidjson::StringBuffer jsonbuffer;

	// make the writer
	rapidjson::Writer<rapidjson::StringBuffer> jsonwriter(jsonbuffer);

	// attach the writer to the json document
	json.Accept(jsonwriter);

	// write the json document out as a string
	return (std::string(jsonbuffer.GetString()));
}

rapidjson::Document & fromJSONString(std::string jsonstring,
										rapidjson::Document & jsondocument) { // NOLINT
	// parse the json into a document
	if (jsondocument.Parse(jsonstring.c_str()).HasParseError()) {
		throw std::invalid_argument("Error parsing JSON string into document.");
	}

	// make sure we got valid json
	if (jsondocument.IsObject() == false) {
		throw std::invalid_argument(
				"JSON string did not parse into valid JSON.");
	} else {
		// return document
		return (jsondocument);
	}
}
}  // namespace hazdevbroker
