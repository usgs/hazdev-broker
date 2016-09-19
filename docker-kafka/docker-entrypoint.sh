#! /bin/bash

# run zookeeper and kafka tasks in the background, and forward SIGTERM manually.
_term () {
  echo 'Caught SIGTERM'
  kill -TERM "$zookeeper" "$kafka"
}
trap _term SIGTERM


# start zookeeper
/kafka/bin/zookeeper-server-start.sh /kafka/config/zookeeper.properties &
zookeeper=$!

# start kafka
/kafka/bin/kafka-server-start.sh /kafka/config/server.properties &
kafka=$!


# keep bash process alive until zookeeper and kafka terminate
wait "$zookeeper" "$kafka"
