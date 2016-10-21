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
1. Build the example jars, by running the command `ant examples` from the /java/
directory (if the examples have not already been built).
2. Ensure that the Kafka Docker image is running, or start it,
[see](../README.md#apache-kafka-docker-image).
3. In the /java/dist/examples directory, edit the `bootstrap.servers` in
consumer.config and producer.config to reflect where the Kafka Docker image is
running.
4. To run the example consumer, from the /java/dist/examples directory run the
command `java -jar ExampleConsumer consumer.config`.
5. To run the example producer, from the /java/dist/examples directory run the
command `java -jar ExampleProducer producer.config`.
