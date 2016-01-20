sbt clean web-stage dist

APP_VERSION=`ls ./target/universal/neta-inbox-*.zip | sed -r 's/(neta-inbox)-(.*).zip/\2/g'`

rm -rf ./output && mkdir -p ./output

unzip ./target/universal/neta-inbox-$APP_VERSION.zip -d ./output/

docker build --no-cache --rm -t cimadai/neta-inbox:$APP_VERSION .

