FROM ubuntu:14.04
MAINTAINER Daisuke Shiamda

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
ENV SCALA_VERSION 2.11.7
ENV SBT_VERSION 0.13.8

RUN \
  apt-get update && \
  apt-get install -y nodejs npm ruby curl git

# Install locale ja_JP
RUN \
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

# Install Scala
RUN \
  cd /root && \
  curl -o scala-$SCALA_VERSION.tgz http://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz && \
  tar -xf scala-$SCALA_VERSION.tgz && \
  rm scala-$SCALA_VERSION.tgz && \
  echo >> /root/.bashrc && \
  echo 'export PATH=~/scala-$SCALA_VERSION/bin:$PATH' >> /root/.bashrc

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt

RUN npm install -g typescript
RUN gem install sass
RUN ln -s /usr/bin/nodejs /usr/bin/node
RUN curl -kL https://bootstrap.pypa.io/get-pip.py | python
RUN pip install --user codecov
RUN echo 'export PATH=$PATH:~/.local/bin/' >> /root/.bashrc

# publish local scala-slack
RUN git clone https://github.com/flyberry-capital/scala-slack.git /tmp/scala-slack
WORKDIR /tmp/scala-slack
RUN sed -i -e "s/-SNAPSHOT//g" build.sbt
RUN sbt publishLocal

WORKDIR /
RUN rm -rf /tmp/scala-slack
RUN rm -rf ~/.ivy2/cache/
RUN apt-get clean && rm -rf /var/cache/apt/* && rm -rf /var/lib/apt/lists/*

