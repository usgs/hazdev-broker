# hazdev-broker

[![Build Status](https://travis-ci.org/usgs/hazdev-broker.svg?branch=master)](https://travis-ci.org/usgs/hazdev-broker)

Small Rapid Data Distribution via a Message Broker

The US Geological Survey (USGS) National Earthquake Information Center (NEIC)
has designed a system to distribute small and rapid earthquake data (often
unassociated) such as near-real-time picks, beam back azimuth/slowness,
detections from cross-correlation and other detection methods detections,
and other appropriate messages between seismic acquisition and processing
systems. This system uses the [Apache Kafka](http://kafka.apache.org/)
open source message broker to distribute messages.

This repository contains the client libraries used to connect, send to, and
receive from the NEIC Kafka Broker Cluster.

[License](LICENSE.md)

## Supported Languages:
hazdev-broker currently provides client libraries written in C++11 and Java 1.7,
and an Apache Kafka docker image.

## Design
The design of hazdev-broker is outlined in the [design](design-docs/design.md)
and [requirements](design-docs/requirements.md) documents.

# Getting Started

## C++11 library
* See the [C++ README](cpp/README.md).

## Java 1.7 jar
* See the [Java README](java/README.md).

## Apache Kafka Docker image

Files to build an Apache Kafka docker image are stored in `docker-kafka`.

http://kafka.apache.org/

### To run the image:
```
docker run -d --name docker-kafka -p 2181:2181 -p 9092:9092 -e KAFKA_ADVERTISED_HOST_NAME=`hostname` usgs/docker-kafka
```

- `-d` - run container in background
- `--name docker-kafka` assign name to container
- `-p 2181:2181` expose zookeeper port
- `-p 9092:9092` expose kafka broker port
- ``` -e KAFKA_ADVERTISED_HOST_NAME=`hostname` ``` specify hostname to be used by clients
- `usgs/docker-kafka` the image on Docker Hub

### To restart the container
```
docker restart docker-kafka
```

### To build the image:
```
cd docker-kafka
docker build -t usgs/docker-kafka:latest .
```
