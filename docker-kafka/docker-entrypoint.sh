#! /bin/bash

# run zookeeper and kafka tasks in the background, and forward SIGTERM manually.
_term () {
  echo 'Caught SIGTERM'
  kill -TERM "$zookeeper" "$kafka"
}
trap _term SIGTERM


# advertised host name is sent to clients that are connecting,
# and should be configured to the public IP/hostname of the docker host.
KAFKA_ADVERTISED_HOST_NAME=${KAFKA_ADVERTISED_HOST_NAME:-`hostname`}


# start zookeeper
/kafka/bin/zookeeper-server-start.sh \
    /kafka/config/zookeeper.properties \
    &
zookeeper=$!

# start kafka
/kafka/bin/kafka-server-start.sh \
    /kafka/config/server.properties \
    --override "advertised.host.name=${KAFKA_ADVERTISED_HOST_NAME}" \
    &
kafka=$!


# keep bash process alive until zookeeper and kafka terminate
wait "$zookeeper" "$kafka"
