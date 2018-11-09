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

# ----- EXTERNAL LIBRARIES ----- #
# rapidjson
include(${CMAKE_DIR}/include_rapidjson.cmake)

# ----- BROKER LIBRARIES ----- #
# librdkafka
include(${CMAKE_DIR}/include_librdkafka.cmake)

# ----- SET PROJECT INCLUDE DIRECTORIES ----- #
include_directories(${PROJECT_SOURCE_DIR}/include)

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
