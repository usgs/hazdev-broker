cmake_minimum_required (VERSION 3.4)

# ----- VERSION NUMBER----- #
set(VERSION_MAJOR   0)
set(VERSION_MINOR   1)
set(VERSION_PATCH   0)

# ----- PROJECT ----- #
project (HazdevBroker VERSION ${VERSION_MAJOR}.${VERSION_MINOR}.${VERSION_PATCH})

# ----- CMAKE INCLUDES ----- #
include(ExternalProject)
include(internal_utils.cmake)

fix_default_compiler_settings()  # Defined in internal_utils.cmake.

# ----- SET FOR C++14 STANDARD ----- #
if (NOT MSVC)
    set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++14")
endif (NOT MSVC)

# ----- CMAKE CONFIG HEADER ----- #
# pass some of the CMake settings
# to the source code
configure_file (
    "${PROJECT_SOURCE_DIR}/HazdevBrokerCMakeConfig.h.in"
    "${PROJECT_BINARY_DIR}/HazdevBrokerCMakeConfig.h"
)

# ----- EXTERNAL LIBRARIES ----- #
# librdkafka
set(LIBRDKAFKA_PATH "${CURRENT_SOURCE_DIR}/../../rdkafka/" CACHE PATH "Path to the rdkafka library")
set(LIBKAFKA_C_INC ${LIBRDKAFKA_PATH}/src)
set(LIBKAFKA_CPP_INC ${LIBRDKAFKA_PATH}/src-cpp)

# rapidjson
set(RAPIDJSON_PATH "${CURRENT_SOURCE_DIR}/../lib/rapidjson" CACHE PATH "Path to rapidjson")

# ----- SET INCLUDE DIRECTORIES ----- #
include_directories(${PROJECT_BINARY_DIR})
include_directories(${PROJECT_SOURCE_DIR}/include)
include_directories(${RAPIDJSON_PATH}/..)
include_directories(${RAPIDJSON_PATH})
include_directories(${LIBKAFKA_C_INC})
include_directories(${LIBKAFKA_CPP_INC})

# ----- SET SOURCE FILES ----- #
file(GLOB SRCS ${PROJECT_SOURCE_DIR}/src/*.cpp)

# ----- SET HEADER FILES ----- #
file(GLOB HDRS ${PROJECT_SOURCE_DIR}/include/*.h)

# ----- CREATE LIBRARY ----- #
add_library (HazdevBroker STATIC ${SRCS} ${HDRS})

# ----- TARGET PROPERTIES ----- #
set_target_properties(HazdevBroker PROPERTIES
    OUTPUT_NAME hazdevbroker)

    # ----- GENERATE ----- #
    include(GenerateExportHeader)
    generate_export_header(HazdevBroker)

    # ----- GLOBAL INCLUDES ----- #
    target_include_directories(
        HazdevBroker PUBLIC
        "$<BUILD_INTERFACE:${PROJECT_SOURCE_DIR}>"
        "$<BUILD_INTERFACE:${CMAKE_CURRENT_BINARY_DIR}>"
    )

# ----- CREATE EXAMPLES ----- #
option(BUILD_EXAMPLES "Build examples." OFF)
if (BUILD_EXAMPLES)

    # look for the kafka c lib
    find_library(LIBRDKAFKA_C_LIB
        NAMES ${CMAKE_SHARED_LIBRARY_PREFIX}rdkafka${CMAKE_SHARED_LIBRARY_SUFFIX}
        PATHS ${LIBRDKAFKA_PATH}/src
    )

    # check to see if we found it
    if (LIBRDKAFKA_C_LIB STREQUAL "LIBRDKAFKA_C_LIB-NOTFOUND")
        message (FATAL_ERROR "Couldn't find the librdkafka c library")
    else ()
        set (EXAMPLE_LIBS ${EXAMPLE_LIBS} ${LIBRDKAFKA_C_LIB})
    endif ()

    # look for the kafka cpp lib
    find_library(LIBRDKAFKA_CPP_LIB
        NAMES ${CMAKE_SHARED_LIBRARY_PREFIX}rdkafka++${CMAKE_SHARED_LIBRARY_SUFFIX}
        PATHS ${LIBRDKAFKA_PATH}/src-cpp
    )

    # check to see if we found it
    if (LIBRDKAFKA_CPP_LIB STREQUAL "LIBRDKAFKA_CPP_LIB-NOTFOUND")
        message (FATAL_ERROR "Couldn't find the librdkafka cpp library")
    else ()
        set (EXAMPLE_LIBS ${EXAMPLE_LIBS} ${LIBRDKAFKA_CPP_LIB})
    endif ()


    set (EXAMPLE_LIBS ${EXAMPLE_LIBS} HazdevBroker)

    # ----- CREATE EXECUTABLES ----- #
    add_executable (example_consumer ${PROJECT_SOURCE_DIR}/examples/example_consumer.cpp)
    target_link_libraries(example_consumer ${LIBRDKAFKA_C_LIB})
    target_link_libraries(example_consumer ${LIBRDKAFKA_CPP_LIB})
    target_link_libraries(example_consumer HazdevBroker)
    install(TARGETS example_consumer DESTINATION examples)
    install(FILES ${PROJECT_SOURCE_DIR}/examples/consumer.config DESTINATION examples)

    add_executable (example_producer ${PROJECT_SOURCE_DIR}/examples/example_producer.cpp)
    target_link_libraries(example_producer ${LIBRDKAFKA_C_LIB})
    target_link_libraries(example_producer ${LIBRDKAFKA_CPP_LIB})
    target_link_libraries(example_producer HazdevBroker)
    install(TARGETS example_producer DESTINATION examples)
    install(FILES ${PROJECT_SOURCE_DIR}/examples/producer.config DESTINATION examples)
endif ()

# ----- DOCUMENTATION ----- #
option(GENERATE_DOCUMENTATION "Create and install the HTML based API documentation (requires Doxygen)" OFF)

if(GENERATE_DOCUMENTATION)

    # ----- LOOK FOR DOXYGEN ----- #
    find_package(Doxygen REQUIRED)

    set(doxyfile_in ${CMAKE_CURRENT_SOURCE_DIR}/Doxyfile.in)
    set(doxyfile ${CMAKE_CURRENT_BINARY_DIR}/Doxyfile)

    configure_file(${doxyfile_in} ${doxyfile} @ONLY)

    add_custom_target(doc ALL
        COMMAND ${DOXYGEN_EXECUTABLE} ${doxyfile}
        WORKING_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}
        COMMENT "Generating API documentation with Doxygen"
        VERBATIM)

    install(DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/html DESTINATION doc)

endif()

# ----- INSTALL RULES ----- #
# Layout. This works for all platforms:
#   * <prefix>/lib/<PROJECT-NAME>
#   * <prefix>/lib/
#   * <prefix>/include/
set(config_install_dir "lib/${PROJECT_NAME}")
set(include_install_dir "include")

set(generated_dir "${CMAKE_CURRENT_BINARY_DIR}/generated")

# ----- Configuration ----- #
set(version_config "${generated_dir}/${PROJECT_NAME}ConfigVersion.cmake")
set(project_config "${generated_dir}/${PROJECT_NAME}Config.cmake")
set(targets_export_name "${PROJECT_NAME}Targets")
set(namespace "${PROJECT_NAME}::")

# Include module with function 'write_basic_package_version_file'
include(CMakePackageConfigHelpers)

# Configure '<PROJECT-NAME>ConfigVersion.cmake'
# Note: PROJECT_VERSION is used as a VERSION
write_basic_package_version_file(
    "${version_config}" COMPATIBILITY SameMajorVersion
)

# Configure '<PROJECT-NAME>Config.cmake'
# Use variables:
#   * targets_export_name
#   * PROJECT_NAME
configure_package_config_file(
    "Config.cmake.in"
    "${project_config}"
    INSTALL_DESTINATION "${config_install_dir}"
)

# Targets:
#   * <prefix>/lib/libHazdevBroker.a
#   * header location after install: <prefix>/include/*.h
install(
    TARGETS HazdevBroker
    EXPORT "${targets_export_name}"
    LIBRARY DESTINATION "lib"
    ARCHIVE DESTINATION "lib"
    RUNTIME DESTINATION "bin"
    INCLUDES DESTINATION "${include_install_dir}"
)

# Headers:
#   * *.h-> <prefix>/include/*.h
install(
    FILES ${HDRS}
    DESTINATION "${include_install_dir}/${PROJECT_NAME}"
)

# rapidjson
install(
    DIRECTORY ${RAPIDJSON_PATH}
    DESTINATION "${include_install_dir}"
)

# Export headers:
#   * ${CMAKE_CURRENT_BINARY_DIR}/HazdevBroker_export.h -> <prefix>/include/HazdevBroker_export.h
install(
    FILES
        "${CMAKE_CURRENT_BINARY_DIR}/HazdevBroker_export.h"
    DESTINATION "${include_install_dir}"
)

# Config
#   * <prefix>/lib/HazdevBroker/HazdevBrokerConfig.cmake
#   * <prefix>/lib/HazdevBroker/HazdevBrokerConfigVersion.cmake
install(
    FILES "${project_config}" "${version_config}"
    DESTINATION "${config_install_dir}"
)

# Config
#   * <prefix>/lib/HazdevBroker/HazdevBrokerTargets.cmake
install(
    EXPORT "${targets_export_name}"
    NAMESPACE "${namespace}"
    DESTINATION "${config_install_dir}"
)
