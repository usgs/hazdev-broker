FROM debian:jessie

MAINTAINER Jeremy Fee <jmfee@usgs.gov>
LABEL usgs.docker-kafka.version=v0.10.0.1

ENV DEBIAN_FRONTEND=noninteractive


# update os
RUN apt-get update --fix-missing && \
    apt-get install -y --no-install-recommends \
        bzip2 \
        ca-certificates \
        curl \
        openjdk-7-jdk && \
    apt-get clean


# download kafka binary distribution
RUN cd /tmp && \
    curl -O http://mirrors.gigenet.com/apache/kafka/0.10.0.1/kafka_2.11-0.10.0.1.tgz && \
    tar -xzvf kafka* && \
    rm -rf kafka*.tgz && \
    mv kafka* /kafka


# create user, and grant write permissions selectively
RUN useradd \
        -c 'Docker image user' \
        -m \
        -r \
        -s /sbin/nologin \
         docker_user && \
    mkdir /kafka/logs && \
    chown docker_user /kafka/logs


# copy docker-entrypoint.sh
COPY . /home/docker_user/


USER docker_user
WORKDIR /home/docker_user
ENTRYPOINT [ "/home/docker_user/docker-entrypoint.sh" ]
