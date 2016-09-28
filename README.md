# hazdev-broker
Small Rapid Data Distribution via a Message Broker



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


### To build the image:
```
cd docker-kafka
docker build -t usgs/docker-kafka:latest .
```
