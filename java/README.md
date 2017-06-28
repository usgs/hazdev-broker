# Java 1.7 Hazdev-Broker Library

This is the Java implementation of the library used to communicate with the
Hazdev Broker Cluster.

Dependencies
------
* Hazdev-Broker utilizes [JSON](www.json.org) for configuration formatting.
* Hazdev-Broker utilizes [Apache Kafka](http://kafka.apache.org/) to
communicate with the broker cluster.
* Hazdev-Broker was written in Java 1.7
* Hazdev-Broker is built with [Apache Ant](http://ant.apache.org/), and was
written using Eclipse.  Eclipse project files, source files, and ant build.xml
are included
* Hazdev-Broker utilizes [json.simple](http://code.google.com/p/json-simple/) to
format, parse, and write JSON.  A copy of the json.simple jar is included in
this project.

All dependencies are included in the hazdev-broker.jar.

Building
------
The steps to get and build hazdev-broker.jar using ant are as follows:
1. Clone hazdev-broker.
2. Open a command window and change directories to /java/
3. To build the jar file, run the command `ant jar`
4. To build the example jars, run the command `ant examples`.
5. To generate javadocs, run the command ant javadoc
6. To compile, generate javadocs, build jar, and build examples, run the command
`ant all`

Using
-----
Once you are able to build the hazdev-broker jar, simply include the jar file in
your application.

Examples
-----
An example consumer and producer are included with the java implementation of
the Hazdev-Broker library.  The steps to use these examples are:

Build the example jars, by running the command `ant examples` from the /java/
directory (if the examples have not already been built).

Ensure that the Kafka Docker image is running, or start it with the command:
```
docker run -d --name docker-kafka -p 2181:2181 -p 9092:9092 -e KAFKA_ADVERTISED_HOST_NAME=`hostname` usgs/docker-kafka
```

See [here](../README.md) for additional information about the Kafka Docker image.

Run the example producer, from the /java/dist/examples directory in a new
window, run the command `java -jar ExampleProducer.jar producer.config`.  Please
note that since the producer creates the example topic (default is "test"), the
producer must be started first in this example.  The example defaults to using
the Kafka Docker image on localhost (edit the `bootstrap.servers` in  the
producer.config if this is not true).

To run the example consumer, from the /java/dist/examples directory in a new
window, run the command `java -jar ExampleConsumer.jar consumer.config`. The
example defaults to using the Kafka Docker image on localhost (edit the
`bootstrap.servers` in  the consumer.config if this is not true).

Type messages into the example producer window, and observe the messages reported
in the example consumer window.

Consumer client
-----

The Hazdev-Broker Jar includes a file based consumer client that consumes text
messages from one or more given Kafka Topics, and writes them out as files with
a given extension at a given location.

**Configuration**

An [example consumer client configuration file](config/consumerclient/consumerclient.config)
is provided with the Hazdev-Broker Jar.  Important consumer configuration
entries are as follows:

Required Configuration:
* FileExtension - Specifies the file extension to use.
* OutputDirectory - Specifies the output directory to use.
* HazdevBrokerConfig - Specifies the Hazdev-Broker configuration to connect to
the Kafka server.
* TopicList - Specifies one or more topics to listen to on the Kafka server.

Optional Configuration:
* MessagesPerFile - Specifies the maximum number of messages per file, unless
TimePerFile is specified, the default is 1 message per file.
* TimePerFile - Specifies the maximum amount of time in seconds to wait before
writing a file if there are unwritten messages. This option is disabled by
default.
* FileName - Specifies a file name to use when generating output files.
* Log4JConfigFile - Specifies a log4j properties file to use for logging.

An [example producer client configuration file](config/producerclient/producerclient.config)
is provided with the Hazdev-Broker Jar.  Important producer configuration
entries are as follows:

Required Configuration:
* FileExtension - Specifies the file extension to use.
* InputDirectory - Specifies the output directory to use.
* HazdevBrokerConfig - Specifies the Hazdev-Broker configuration to connect to
the Kafka server.
* Topic - Specifies the topic to write to on the Kafka server.

Optional Configuration:
* ArchiveDirectory - Specifies the archive directory to use. If not specified,
input files are deleted once processed.
* TimePerFile - Specifies the maximum amount of time in seconds to wait between
processing input files.
* Log4JConfigFile - Specifies a log4j properties file to use for logging.

**Logging**

The consumer client uses log4j for logging, an [example log4j properties file](config/consumerclient/consumerclient.log4j.properties)
is included with the Hazdev-Broker Jar.  For more information on configuring
log4j, see [here](http://logging.apache.org/log4j/1.2/manual.html).

**Using**

To run the consumer client, run the command `java -jar hazdev-broker.jar ConsumerClient consumerclient.config`.

To run the producer client, run the command `java -jar hazdev-broker.jar ProducerClient producerclient.config`.
