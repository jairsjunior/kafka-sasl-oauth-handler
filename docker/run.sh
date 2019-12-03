#!/bin/sh

docker rm --force rest-proxy
docker-compose -f docker-compose-slim-restproxy.yml up -d

docker logs -f rest-proxy