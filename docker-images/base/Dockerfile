FROM ubuntu:14.04
MAINTAINER Daisuke Shiamda

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# Install locale ja_JP
RUN \
  apt-get update && \
  apt-get install -y language-pack-ja && \
  export LANG=ja_JP.UTF-8  && \
  update-locale LANG=ja_JP.UTF-8

# Install Java
RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  apt-get install -y software-properties-common && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer

# Install supervisor
RUN apt-get install -y supervisor

# Cleanup
RUN apt-get clean && rm -rf /var/cache/apt/* && rm -rf /var/lib/apt/lists/*

