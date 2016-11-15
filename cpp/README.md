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

1. Clone hazdev-broker.
2. Open a command window and change directories to `lib/``
3. Extract the `librdkafka.zip` and `rapidjson.zip` to `lib/librdkafka/`` and
`lib/rapidjson/``
3. Change directories to `cpp/``
4. Make a build directory `mkdir build`
5. Change to the build directory `cd build`
6. Run CMake `cmake ..`.
7. If you are on a \*nix system, you should now see a Makefile in the current
directory.  Just type 'make' to build detection-formats.  'make install' will
copy the include files and libraries to the location defined by
`CMAKE_INSTALL_PREFIX`.
8. If you are on Windows and have Visual Studio installed, a `HazdevBroker.sln`
file and several `.vcproj` files will be created.  You can then build them using
Visual Studio.  Building the INSTALL project will copy the include files and
libraries to the location defined by `CMAKE_INSTALL_PREFIX` (add
`-DCMAKE_INSTALL_PREFIX=<path to install location>` to cmake call to define
install location).
9. Note that for \*nix you must generate separate build directories for x86 vs
x64 compilation specifying the appropriate generator `cmake -G <generator> ..`

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

Build the examples

1. Build the librdkafka library in `lib/librdkafka/` by entering the command
`./configure` followed by the commands `make`
2. Install the librdkafka library by running the command `sudo make install`.
This will install rdkafaka to `/usr/local/lib` and `/usr/local/include`.
3. Build the example programs, by ensuring that `BUILD_EXAMPLES` is enabled and
librdkafka is referenced as part of the CMake (add `-DBUILD_EXAMPLES=true -DRDKAFKA=/usr/local/lib/librdkafka++.dylib -DRDKAFKA_PATH=/usr/local/include/librdkafka `).

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
