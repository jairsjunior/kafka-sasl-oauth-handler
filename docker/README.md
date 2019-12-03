# Getting Started

## Build this project jar file

Our first step is build the jar file of our project. You can use your IDE of preference or the maven client to assembly the jar file.
The final jar file needs to be at the root directory of this project at the folder target/

## Change the content of `kafka_server_jaas.conf`

First, we need to change the file at folder cp-enterprise-kafka-oauth/config/kafka_server_jaas.conf, filling the propertie `ims.token.validation.url` with the complete url of your validation server.

Example:
- ims.token.validation.url="http://yourserver.com/ims/validate_token/v1"

## Build the Containers

To build the containers needed to test our implementation, you will execute the shell script `build.sh`. At the end of this execution
the containers needed will be builded.

## Run the `docker-compose-slim-resproxy.yml` file

To run all the containers needed to make our tests, you will execute the shell script `run.sh`. The result of this execution is
run five containers(zookeeper,kafka-broker,schema-registry,control-center and the rest-proxy) with all environments configured.
After run all this conteiners, the shell script tail the logs of the rest-proxy container.

## Executing Tests

Here, the first step is get your *access_token* using your credentials with your IMS Authentication Server. With the access_token in
hands you can replace the ENVIRONMENT VARIABLE at the test.sh file named BEARER_TOKEN and save the file.

You can access the control-center and create a topic called **test-topic**

After this, you can run the `test.sh` file. This file had some basic operations:
    - Produce one message to an topic
    - Create a consumer for JSON data
    - Set the topic that will be consumed
    - Consume the messages of the topic
    - Delete the consumer instance
