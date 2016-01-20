FROM cimadai/neta-inbox-base
MAINTAINER Daisuke Shiamda
ENV SRC_DIR=/var/neta-inbox

EXPOSE 9090

RUN mkdir -p $SRC_DIR/log
COPY ./output/neta-inbox-* /var/neta-inbox/
COPY script/neta_inbox_docker_supervisor.conf /etc/supervisor.conf

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor.conf"]

