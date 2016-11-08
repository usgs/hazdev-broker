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
window, run the command `java -jar ExampleProducer producer.config`.  Please
note that since the producer creates the example topic (default is "test"), the
producer must be started first in this example.  The example defaults to using
the Kafka Docker image on localhost (edit the `bootstrap.servers` in  the
producer.config if this is not true).

To run the example consumer, from the /java/dist/examples directory in a new
window, run the command `java -jar ExampleConsumer consumer.config`. The example
defaults to using the Kafka Docker image on localhost (edit the
`bootstrap.servers` in  the consumer.config if this is not true).

Type messages into the producer client window, and observe the messages reported
in the consumer client window.
