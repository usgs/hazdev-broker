# C++ 11 Hazdev-Broker Library

This is the C++11 implementation of the library used to communicate with the
Hazdev Broker Cluster.

Dependencies
------
* Hazdev-Broker utilizes [JSON](www.json.org) for formatting.
* Hazdev-Broker utilizes [librdkafka](https://github.com/edenhill/librdkafka/)
to communicate with the broker cluster.  A copy of librdkafka is included in
this project.
* Hazdev-Broker uses a [CMake](http://www.cmake.org/) build script
([CMakeLists.txt](CMakeLists.txt)) for cross platform compilation.  
A copy of CMake is not included in this project
* Hazdev-Broker utilizes [rapidjson](https://github.com/miloyip/rapidjson)
to format, parse, and write JSON.  A copy of include/rapidjson is included in
this project.

The librdkafa and related dependencies (such as ssl libraries) are linked
dynamically and are not included in the detection-formats.lib.

Building
------
The steps to get and build hazdev-broker using CMake are as follows:
1. Clone detection-formats.
2. Open a command window and change directories to /cpp/
3. Make a build directory `mkdir build`
4. Change to the build directory `cd build`
5. Run CMake `cmake ..`.
6. If you are on a \*nix system, you should now see a Makefile in the current
directory.  Just type 'make' to build detection-formats.  'make install' will
copy the include files and libraries to the location defined by
`CMAKE_INSTALL_PREFIX`.
7. If you are on Windows and have Visual Studio installed, a `HazdevBroker.sln`
file and several `.vcproj` files will be created.  You can then build them using
Visual Studio.  Building the INSTALL project will copy the include files and
libraries to the location defined by `CMAKE_INSTALL_PREFIX` (add
`-DCMAKE_INSTALL_PREFIX=<path to install location>` to cmake call to define
install location).
8. Note that for \*nix you must generate separate build directories for x86 vs
x64 compilation specifying the appropriate generator `cmake -G <generator> ..`.

Using
------
Once you are able to build the hazdev-broker library, you should create a
project or build target for your program. Make sure you have the location of
Consumer.h or Producer.h in the header search path. Set program to link with the
hazdev-broker library.

Examples
-----
An example consumer and producer are included with the C++11 implementation of
the Hazdev-Broker library.  The steps to use these examples are:
1. Build the example programs, by ensureing that `BUILD_EXAMPLES` is enabled via
Cmake (add `-BUILD_EXAMPLES=true`).
2. Ensure that the Kafka Docker image is running, or start it,
[see](../README.md#apache-kafka-docker-image).
3. In the /cpp/dist/examples directory, edit the `metadata.broker.list` in
consumer.config and producer.config to reflect where the Kafka Docker image is
running.
4. To run the example consumer, from the /cpp/dist/examples directory run the
command `example_consumer consumer.config`.
5. To run the example producer, from the /cpp/dist/examples directory run the
command `example_producer producer.config`.
