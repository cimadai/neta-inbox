FROM cimadai/neta-inbox-base
MAINTAINER Daisuke Shiamda
RUN apt-get update && apt-get install -y nodejs npm ruby
RUN npm install -g typescript
RUN gem install sass
RUN ln -s /usr/bin/nodejs /usr/bin/node
RUN curl -kL https://bootstrap.pypa.io/get-pip.py | python
RUN pip install --user codecov
RUN export PATH=$PATH:~/.local/bin/
RUN apt-get clean && rm -rf /var/cache/apt/* && rm -rf /var/lib/apt/lists/*

