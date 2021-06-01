# C++ 11 Hazdev-Broker Library

This is the C++11 implementation of the library used to communicate with the
Hazdev Broker Cluster.

The library supports sending (producing) and receiving (consuming) messages
to/from an Apache Kafka Broker Cluster. The library also supports heartbeat
messages (sent by the producer) to enable better monitoring of broker
communication.

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
to format, parse, and write JSON.  A copy of rapidjson is included in
this project.
* Hazdev-Broker optionally uses [doxygen](http://www.stack.nl/~dimitri/doxygen/) 
for documentation generation.  A copy of doxygen is optionally downloaded as part 
of the build.
* Hazdev-Broker optionally uses [cpplint](https://github.com/google/styleguide/tree/gh-pages/cpplint)
to check coding style. A copy of cpplint is included in this project.
* Hazdev-Broker optionally uses [cppcheck](http://cppcheck.sourceforge.net/)
for static code analysis. A copy of cppcheck is **not** included in this project.

The librdkafa and related dependencies (such as ssl libraries) are linked
dynamically and are not included in the HazdevBroker library.

Building
------
The steps to get and build hazdev-broker using CMake are as follows:

1. Clone hazdev-broker.
2. Open a command window and change directories to `lib/`
3. Extract the `librdkafka.zip` to `lib/librdkafka/`
4. Change directories to `lib/librdkafka/`
5. Run the command `./configure`
6. Run the command `./make`
7. Run the command `./make install`
8. Change directories to `hazdev-broker/cpp/`
9. Make a build directory `mkdir build`
10. Change to the build directory `cd build`
11. Run CMake `cmake ..`.
12. If you are on a \*nix system, you should now see a Makefile in the current
directory.  Just type `make` to build hazdev-broker. `make doc` will generate
the documentation if doxygen is installed on the system. `make install` will
copy the include files and libraries to the location defined by
`CMAKE_INSTALL_PREFIX`. You can modify the installation target by adding:``` -DCMAKE_INSTALL_PREFIX=../cpp/dist ```
to the cmake command.
13. If you are on Windows and have Visual Studio installed, a `HazdevBroker.sln`
file and several `.vcproj` files will be created.  You can then build them using
Visual Studio.  Building the INSTALL project will copy the include files and
libraries to the location defined by `CMAKE_INSTALL_PREFIX`. You can modify the
installation target by adding:``` -DCMAKE_INSTALL_PREFIX=../cpp/dist ```
to the cmake command.
14. Note that for \*nix you must generate separate build directories for x86 vs
x64 compilation specifying the appropriate generator `cmake -G <generator> ..`

Using
------
Once you are able to build the hazdev-broker library, you should create a
project or build target for your program. Make sure you have the location of
Consumer.h or Producer.h in the header search path. Set program to link with the
hazdev-broker library.  Also make sure that you have built the rdkafka library
(`lib/librdkafka/`), and include it's libraries in your path.  See the examples
section for instructions on building the librdkafka library.

Examples
-----
Example consumer and producer applications are included with the C++11
implementation of the Hazdev-Broker library.  The steps to use these examples
are:

Build the examples

1. Open a command window and change directories to `lib/`
2. Build the librdkafka library in `lib/librdkafka/` by entering the command
`./configure` followed by the command `make`
3. Install the librdkafka library by running the command `sudo make install`.
This will install rdkafaka to `/usr/local/lib` and `/usr/local/include`.
4. Generate the example makefiles, by ensuring that `BUILD_EXAMPLES` is enabled
and librdkafka is referenced as part of CMake by adding ``` -DBUILD_EXAMPLES=true -DRDKAFKA=/usr/local/lib/librdkafka++.dylib -DRDKAFKA_PATH=/usr/local/include/librdkafka ```
to the cmake command.
5. If you are on a \*nix system, you should see a Makefile in the current
directory.  Just type `make example_consumer` to build the example consumer,
and `make example_producer` to build the example producer, or type `make all` to
build both examples and install them to the the location defined by
`CMAKE_INSTALL_PREFIX`.
6. If you are on Windows and have Visual Studio installed, a `HazdevBroker.sln`
file and several `.vcproj` files will be created.  You can then build them using
Visual Studio.  Building the EXAMPLE_CONSUMER and EXAMPLE_PRODUCER projects will
build the examples.  Building the INSTALL project will copy the examples to the
location defined by `CMAKE_INSTALL_PREFIX`. You can modify the installation 
target by adding:``` -DCMAKE_INSTALL_PREFIX=../cpp/dist ``` to the cmake command.

Start Kafka

Ensure that the Kafka Docker image is running, or start it with the command:
```
docker run -d --name docker-kafka -p 2181:2181 -p 9092:9092 -e KAFKA_ADVERTISED_HOST_NAME=`hostname` usgs/docker-kafka
```

See [here](../README.md) for additional information about the Kafka Docker image.

Run Examples

Run the example producer, from the /cpp/dist/examples directory in a new
window, run the command `example_producer.exe producer.config`.  Please
note that since the producer creates the example topic (default is "test"), the
producer must be started first in this example.  The example defaults to using
the Kafka Docker image on localhost (edit the `metadata.broker.list` in  the
producer.config if this is not true).

To run the example consumer, from the /cpp/dist/examples directory in a new
window, run the command `example_consumer.exe consumer.config`. The
example defaults to using the Kafka Docker image on localhost (edit the
`metadata.broker.list` in  the consumer.config if this is not true).

Type messages into the producer client window, and observe the messages reported
in the consumer client window.
