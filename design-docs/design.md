# hazdev-broker Small Rapid Data Distribution System Design
Last Updated September 14, 2016

The purpose of this document is to outline the design of a system to match the
small, rapid, non-event specific, data distribution requirements outlined in
[this document](requirements.md).

After researching various solutions and technologies, the [Apache Kafka](http://kafka.apache.org/) open
source message broker was selected as the base of the small rapid data distribution
system due to the following capabilities:
* Kafka is optimized for small messages, best performance occurs with 1 KB to 1
MB messages.
* Kafka supports extremely high performance, with benchmarked publishing speeds
of more than 165,000 messages per second.
* Kafka preserves ordered delivery of messages.
* Kafka has the ability to support multiple producers to, and consumers from a
single message thread.
* Kafka supports a distributed broker cluster for redundancy.
* Kafka supports a wide variety of software clients including Java, C/C++,
python, node, and others.
* Kafka includes supports using SSL and SASL for encryption and authentication.
* Kafka supports recording per consumer per topic offsets to support
backfilling.
* Kafka supports the live creation of new message topics to distribute new
message types.

## Design

At a high level, the small data distribution system (like any Kafka
implementation) will look like this:

![Kafka Design](producer_consumer.png)

This means that the design can be broken into two aspects, the producer/consumer
client, and the Kafka broker cluster.

## Producer/Consumer Client

While Kafka provides Java and C/C++ producer/consumer clients with relatively
simple implementations (the C/C++ client is less simple than the Java), they use
different systems for configuring the producer/consumer (the Java client uses
  java properties, the C/C++ client uses a custom class somewhat based on java
  properties).  Accordingly classes should be developed that abstract the
  configuration and complexity of setting up the Java and C/C++clients with the
  goal of the same configuration being usable for both java and C/C++ clients.
  The classes should also include simple abstractions of the sending/receiving
  message functions.

The configuration will be abstracted such that a common, JSON formatted,
configuration string can be used for both the Java and C/C++ implementations.
The JSON configuration will consist of a configuration type identifier, and an
array of Kafka configuration key/value pairs.

```json
{
    "Type" : "KafkaProducerConfig",
    "Properties" :
    {
        "bootstrap.servers" : "localhost:9092",
        "acks" : "all",
        "retries" : 0,
        "acks" : "all",
        "batch.size" : 16384,
        "linger.ms" : 1,
        "acks" : "all"
    }
}
```

The complexity setting up a Kafka connection (primarily C/C++) will be
abstracted such that a user of the client libraries will only need to create a
Kafka class and pass in the configuration string to start using the distribution
system.  The clients will be implemented with send/poll methods using byte
arrays to support arbitrary binary data.  Methods utilizing the send/poll byte
methods will also be implemented to support the most common use case, sending
string formatted messages.

The sending/receiving of messages will be abstracted such that the sending of a
message to Kafka will use the following interfaces:

`void send(String topic, byte[] data)`

Where the String `topic` identifies the message thread, the byte array `data`
contains the data to be sent, and the string message contains the message.

`void sendString(String topic, String message)`

Where the string `topic` identifies the message thread, the String `message`
contains the message to be sent,.

Whether a Kafka send is asynchronous or not is controlled by a configuration
option in the producer config (producer.type).  The default is to send messages
synchronously.  However queuing a message for sending is always asynchronous.

The receiving of a message will use the following interfaces:

`void subscribe(String topic)`

Where the string `topic` identifies the message thread.

`ArrayList<byte[]> poll(int timeout)`

Where the integer timeout indicates the amount of time in milliseconds to wait
for data, and returns an arraylist of byte arrays containing the data since the
last time Kafka was polled, or an empty list if no messages are available.

`ArrayList<String> pollString(int timeout)`

Where the integer timeout indicates the amount of time in milliseconds to wait
for a message, and returns an arraylist of Strings containing the messages since
the last time Kafka was polled, or an empty list if no messages are available.

Kafka does not natively support waiting an arbitrarily long time for a message
while polling, so a negative timeout value passed to the poll functions
indicates the client should wait Long.MAX_VALUE for the next message in order
to support this functionality.

## Kafka Broker Cluster

The process of setting up and configuring a Kafka broker cluster, including
configuring the associated zookeeper cluster used to maintain coordination
broker between brokers in the Kafka cluster, needs to be documented, as well as
basic administrative tasks (setting up new message threads, etc).

A docker image exists for running a Kafka 0.10.0.1 and zookeeper in single node
(see [kafka-docker](https://github.com/wurstmeister/kafka-docker)), however it
is unclear whether the docker image would be appropriate for a production fault
tolerant Kafka cluster.  There is some guidance around running zookeeper in
isolation from other processes, and not running it on virtual hardware. (See “6.7 ZooKeeper” in [Basic Kafka Operations](https://kafka.apache.org/081/ops.html))

It is not recommended to run a single Kafka cluster that spans multiple
data-centers (Denver - Sioux Falls) due to replication latencies.  Another
concern is that if the network partitions, splitting a broker cluster (for
example, brokers/zookeepers in Denver and Sioux Falls and the Denver Gateway
fails) neither Kafka nor Zookeeper will remain available.  Tools exist for
mirroring data from one local Kafka cluster to a “main” cluster at another
datacenter, but the mirroring is not bidirectional. (See [Datacenters in Kafka Documentation](http://kafka.apache.org/documentation.html#datacenters))

For a proof of concept, the Kafka docker image will be run on the sandbox docker
host.  A data source program (either the edge filterpicker, or a hydra module
monitoring the raypicker feed) will be run to send data into Kafka.  An
instance of GLASS that has been enhanced to connect to Kafka will be run to
consume pick messages and send association messages back into Kafka.

The production design of the Kafka broker cluster will be finalized based on
lessons learned from the proof of concept, however research indicates that at
least 3 zookeeper instances (needs to be an odd number, and needs to support
redundancy, see Clustered (Multi-Server) Setup in
[ZooKeeper Administrator's Guide](https://zookeeper.apache.org/doc/r3.3.3/zookeeperAdmin.html#sc_zkMulitServerSetup))
and at least two brokers (again for redundancy) will be needed.  It is possible
that simply running 3 appropriately configured Kafka docker containers might be
sufficient, but they would need to run on separate docker hosts to preserve
redundancy.

The primary unknown at this time is how much of the Kafka broker design and
configuration is necessary for the small data distribution system requirements,
and how much is to support the standard Kafka performance (which is several
orders of magnitude higher than the current small data distribution system
requirements).
