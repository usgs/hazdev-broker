cmake_minimum_required (VERSION 3.4)

# ----- VERSION NUMBER----- #
set(VERSION_MAJOR   0   CACHE STRING "hazdev-broker major version number.")
set(VERSION_MINOR   1   CACHE STRING "hazdev-broker minor version number.")
set(VERSION_PATCH   0   CACHE STRING "hazdev-broker patch version number.")
mark_as_advanced(VERSION_MAJOR VERSION_MINOR VERSION_PATCH)

# ----- OPTIONS ----- #
# option(test "Build all tests." OFF)

# ----- OVERRIDE TO SET STATIC RUNTIME ON MSVC ----- #
set(CMAKE_USER_MAKE_RULES_OVERRIDE ${CMAKE_CURRENT_SOURCE_DIR}/c_flag_overrides.cmake)
set(CMAKE_USER_MAKE_RULES_OVERRIDE_CXX ${CMAKE_CURRENT_SOURCE_DIR}/cxx_flag_overrides.cmake)

# ----- PROJECT ----- #
project (hazdev-broker)

# ----- SET FOR C++14 STANDARD ----- #
if (NOT MSVC)
    set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++14")
endif (NOT MSVC)

# ----- SET INCLUDE DIRECTORIES ----- #
include_directories(${PROJECT_SOURCE_DIR}/include)
include_directories(${PROJECT_SOURCE_DIR}/src)
include_directories(${PROJECT_SOURCE_DIR}/../lib)
include_directories(${PROJECT_SOURCE_DIR}/../lib/rapidjson)
include_directories(${PROJECT_SOURCE_DIR}/../lib/librdkafka/src)
include_directories(${PROJECT_SOURCE_DIR}/../lib/librdkafka/src-cpp)

# ----- SET SOURCE FILES ----- #
set (SRCS ${PROJECT_SOURCE_DIR}/src/ClientBase.cpp
    ${PROJECT_SOURCE_DIR}/src/Consumer.cpp
    ${PROJECT_SOURCE_DIR}/src/Producer.cpp
)

# ----- SET HEADER FILES ----- #
set (HDRS ${PROJECT_SOURCE_DIR}/include/ClientBase.h
    ${PROJECT_SOURCE_DIR}/include/Consumer.h
    ${PROJECT_SOURCE_DIR}/include/Producer.h
)

# ----- SET DEBUG EXTENSION ----- #
if (CMAKE_BUILD_TYPE MATCHES DEBUG)
    message(STATUS "Target is Debug")
    set(DEBUG_EXTENSION "_d")
else (CMAKE_BUILD_TYPE MATCHES DEBUG)
    message(STATUS "Target is Release")
    set(DEBUG_EXTENSION "")
endif (CMAKE_BUILD_TYPE MATCHES DEBUG)

# ----- SET 64 BIT EXTENSION ----- #
if (CMAKE_SIZEOF_VOID_P EQUAL "8")
    message(STATUS "Target is 64 bits")
    set(PLATFORM_EXTENSION "_x64")
else (CMAKE_SIZEOF_VOID_P EQUAL "8")
    message(STATUS "Target is 32 bits")
    set(PLATFORM_EXTENSION "")
endif (CMAKE_SIZEOF_VOID_P EQUAL "8")

# ----- CREATE LIBRARY ----- #
add_library (hazdev-broker STATIC ${SRCS} ${HDRS})
set_target_properties(hazdev-broker PROPERTIES OUTPUT_NAME hazdev-broker${PLATFORM_EXTENSION}${DEBUG_EXTENSION})

# ----- CREATE EXAMPLES ----- #
option(BUILD_EXAMPLES "Build examples." OFF)
if (BUILD_EXAMPLES)
  set (EXAMPLE_LIBS ${EXAMPLE_LIBS} hazdev-broker)

  # ----- EXTERNAL LIBRARIES ----- #
  # rdkafka
  set(RDKAFKA_PATH "${CURRENT_SOURCE_DIR}/../../rdkafka/cpp/dist" CACHE PATH "Path to the rdkafka library")
  find_library(RDKAFKA
      rdkafka${PLATFORM_EXTENSION}${DEBUG_EXTENSION}
      HINTS "${RDKAFKA_PATH}/lib"
  )
  # check to see if we found it
  if (RDKAFKA STREQUAL "RDKAFKA-NOTFOUND")
      message (FATAL_ERROR "Couldn't find the rdkafka library")
  else ()
      include_directories(${RDKAFKA_PATH})
      set (EXAMPLE_LIBS ${EXAMPLE_LIBS} ${RDKAFKA})
  endif ()

  # ----- CREATE EXECUTABLES ----- #
  add_executable (example_consumer ${PROJECT_SOURCE_DIR}/examples/example_consumer.cpp)
  target_link_libraries (example_consumer ${EXAMPLE_LIBS})
  install(TARGETS example_consumer DESTINATION examples)
  install(FILES ${PROJECT_SOURCE_DIR}/examples/consumer.config DESTINATION examples)

  add_executable (example_producer ${PROJECT_SOURCE_DIR}/examples/example_producer.cpp)
  target_link_libraries (example_producer ${EXAMPLE_LIBS})
  install(TARGETS example_producer DESTINATION examples)
  install(FILES ${PROJECT_SOURCE_DIR}/examples/producer.config DESTINATION examples)
endif ()

#----- DOCUMENTATION ----- #
find_package(Doxygen)
option(BUILD_DOCUMENTATION "Create and install the HTML based API documentation (requires Doxygen)" ${DOXYGEN_FOUND})

if(BUILD_DOCUMENTATION)

     if(DOXYGEN_FOUND)
         set(doxyfile_in ${CMAKE_CURRENT_SOURCE_DIR}/Doxyfile.in)
         set(doxyfile ${CMAKE_CURRENT_BINARY_DIR}/Doxyfile)

        configure_file(${doxyfile_in} ${doxyfile} @ONLY)

        add_custom_target(doc
            COMMAND ${DOXYGEN_EXECUTABLE} ${doxyfile}
            WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}
            COMMENT "Generating API documentation with Doxygen"
            VERBATIM)

        install(DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/html DESTINATION doc)
    endif()
endif()

# ----- INSTALL ----- #
install(TARGETS hazdev-broker DESTINATION lib)
install(DIRECTORY ${PROJECT_SOURCE_DIR}/include DESTINATION ".")
install(DIRECTORY ${PROJECT_SOURCE_DIR}/../lib/rapidjson DESTINATION "include")
