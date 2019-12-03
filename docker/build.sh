#!/bin/sh

rm rest-proxy/libs/kafka*
rm cp-enterprise-kafka-oauth/libs/kafka*
cp ../target/kafka-sasl-oauth-handler-1.0-SNAPSHOT-jar-with-dependencies.jar ./rest-proxy/libs
cp ../target/kafka-sasl-oauth-handler-1.0-SNAPSHOT-jar-with-dependencies.jar ./cp-enterprise-kafka-oauth/libs

docker-compose -f docker-compose-slim-restproxy.yml build --no-cache