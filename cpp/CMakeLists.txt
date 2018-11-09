# hazdev-broker CMake configuration file.
cmake_minimum_required (VERSION 3.4)
set(CMAKE_DIR ${CMAKE_CURRENT_LIST_DIR}/cmake/)

# ----- PROJECT VERSION ----- #
include(${CMAKE_DIR}/version.cmake)

# ----- PROJECT ----- #
project (HazdevBroker VERSION ${PROJECT_VERSION_MAJOR}.${PROJECT_VERSION_MINOR}.${PROJECT_VERSION_PATCH})

#----- BASE FUNCTIONS ----- #
include(${CMAKE_DIR}/base.cmake)

if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release)
endif(NOT CMAKE_BUILD_TYPE)

# ----- CMAKE INCLUDES ----- #
include(ExternalProject)

# ----- SETUP INSTALL LOCATIONS ----- #
set(INSTALL_LOCATION ${CMAKE_INSTALL_PREFIX})

# ----- PREPEND INSTALL LOCATIONS TO MODULE PATH ----- #
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${INSTALL_LOCATION}" )

# ----- OPTIONS ----- #
option(GENERATE_DOCUMENTATION "Create and install the HTML based API documentation" OFF)
option(BUILD_EXAMPLES "Build examples." OFF)
option(RUN_CPPCHECK "Run CPP Checks (requires cppcheck installed)" OFF)
option(RUN_CPPLINT "Run CPP Checks (requires cpplint and python installed)" OFF)
option(GIT_CLONE_PUBLIC "Clone from public git URLs (https)" OFF)

# ----- EXTERNAL TOOLS ----- #
# cpplint
if (RUN_CPPLINT)
    set(PYTHON_PATH "/usr/bin/python" CACHE FILEPATH "Path to python")
    set(CPPLINT_PATH "${CMAKE_CURRENT_LIST_DIR}/lib/cpplint/cpplint.py" CACHE FILEPATH "Path to cpplint")
else()
    set(PYTHON_PATH "")
    set(CPPLINT_PATH "")
endif(RUN_CPPLINT)

# cppcheck
if (RUN_CPPCHECK)
    set(CPPCHECK_PATH "/usr/local/bin/cppcheck" CACHE FILEPATH "Path to cppcheck")
else()
    set(CPPCHECK_PATH "")
endif(RUN_CPPCHECK)

# Doxygen
if (BUILD_DOCUMENTATION)
    # ----- LOOK FOR DOXYGEN ----- #
    find_package(Doxygen)

    # ----- DOWNLOAD DOXYGEN IF NOT FOUND ----- #
    if(NOT DOXYGEN_FOUND)
        MESSAGE(STATUS "Configuring to download Doxygen.")

        if (GIT_CLONE_PUBLIC)
          ExternalProject_Add(
              Doxygen
              GIT_REPOSITORY https://github.com/doxygen/doxygen.git
              GIT_TAG Release_1_8_12
              TIMEOUT 10
              CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=${INSTALL_LOCATION}
          )
        else()
          ExternalProject_Add(
              Doxygen
              GIT_REPOSITORY git@github.com:doxygen/doxygen.git
              GIT_TAG Release_1_8_12
              TIMEOUT 10
              CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=${INSTALL_LOCATION}
          )
        endif()

        set(DOXYGEN_DEPEND "Doxygen")
    endif()
endif(BUILD_DOCUMENTATION)

# ----- EXTERNAL LIBRARIES ----- #
# rapidjson
set(RAPIDJSON_PATH "${CMAKE_CURRENT_LIST_DIR}/lib/rapidjson" CACHE PATH "Path to rapidjson")

# ----- BROKER LIBRARIES ----- #
# rdkafka
set(LIBRDKAFKA_PATH
    "${CMAKE_CURRENT_LIST_DIR}/lib/rdkafka/"
    CACHE PATH "Path to rdkafka")
set(LIBRDKAFKA_C_LIB
    "${CMAKE_CURRENT_LIST_DIR}/lib/rdkafka/src/${CMAKE_SHARED_LIBRARY_PREFIX}rdkafka${CMAKE_SHARED_LIBRARY_SUFFIX}"
    CACHE FILEPATH "Path to the rdkafka c library")
set(LIBRDKAFKA_CPP_LIB
    "${CMAKE_CURRENT_LIST_DIR}/lib/rdkafka/src_cpp/${CMAKE_SHARED_LIBRARY_PREFIX}rdkafka++${CMAKE_SHARED_LIBRARY_SUFFIX}"
    CACHE FILEPATH "Path to the rdkafka cpp library")

# ----- SET PROJECT INCLUDE DIRECTORIES ----- #
include_directories(${PROJECT_SOURCE_DIR}/include)
include_directories(${RAPIDJSON_PATH}/..)
include_directories(${RAPIDJSON_PATH})

# ----- SET SOURCE FILES ----- #
file(GLOB SRCS ${PROJECT_SOURCE_DIR}/src/*.cpp)

# ----- SET HEADER FILES ----- #
file(GLOB HDRS ${PROJECT_SOURCE_DIR}/include/*.h)

# ----- BUILD LIBRARY ----- #
include(${CMAKE_DIR}/build_lib.cmake)

# ----- INSTALL LIBRARY ----- #
include(${CMAKE_DIR}/install_lib.cmake)

# ----- BUILD EXAMPLES ----- #
if (BUILD_EXAMPLES)
    # librdkafka
    include(${CMAKE_DIR}/include_librdkafka.cmake)

    # ----- BUILD EXECUTABLE ----- #
    # WARNING: linking order of libraries matters for G++
    set(EXE_LIBRARIES HazdevBroker ${LIBRDKAFKA_CPP_LIB} ${LIBRDKAFKA_C_LIB})

    # ----- LINIX LIBRARIES ----- #
    if (UNIX AND NOT APPLE)
        set(ZLIB -lz)
        set(LIBDL -ldl)
    endif (UNIX AND NOT APPLE)

    set(EXE_DESTINATION "examples")

    # ----- CREATE EXECUTABLE ----- #
    set(EXE_NAME "example_consumer")
    set(SRCS "${PROJECT_SOURCE_DIR}/examples/example_consumer.cpp")
    include(${CMAKE_DIR}/build_exe.cmake)

    # ----- INSTALL EXECUTABLE ----- #
    set(EXE_PARAMS "${PROJECT_SOURCE_DIR}/examples/consumer.config")
    include(${CMAKE_DIR}/install_exe.cmake)

    # ----- CREATE EXECUTABLE ----- #
    set(EXE_NAME "example_producer")
    set(SRCS "${PROJECT_SOURCE_DIR}/examples/example_producer.cpp")
    include(${CMAKE_DIR}/build_exe.cmake)

    # ----- INSTALL EXECUTABLE ----- #
    set(EXE_PARAMS "${PROJECT_SOURCE_DIR}/examples/producer.config")
    include(${CMAKE_DIR}/install_exe.cmake)
endif ()

# ----- RUN CPPCHECK ----- #
include(${CMAKE_DIR}/cppcheck.cmake)

# ----- RUN CPPLINT ----- #
include(${CMAKE_DIR}/cpplint.cmake)

# ----- GENERATE DOCUMENTATION ----- #
set(DOC_DIRS "${PROJECT_SOURCE_DIR}/include/ ${PROJECT_SOURCE_DIR}/examples/")
include(${CMAKE_DIR}/documentation.cmake)
