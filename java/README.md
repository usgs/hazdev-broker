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

Building
------
The steps to get and build hazdev-broker.jar using ant are as follows:
1. Clone hazdev-broker.
2. Open a command window and change directories to /java/
3. To build the jar file, run the command `ant jar`
4. To build the example jars, run the command `ant examples`.
5. To generate javadocs, run the commant ant javadoc
6. To compile, generate javadocs, build jar, and build examples, run the command
`ant all`

Using
-----
Once you are able to build the hazdev-broker jar, simply include the jar file in
your application.
