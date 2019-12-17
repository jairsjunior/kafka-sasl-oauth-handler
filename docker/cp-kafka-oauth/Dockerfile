FROM confluentinc/cp-kafka:5.3.1

RUN curl https://search.maven.org/remotecontent?filepath=org/jolokia/jolokia-jvm/1.6.2/jolokia-jvm-1.6.2-agent.jar -o /usr/share/java/kafka/jolokia-jvm-1.6.2-agent.jar

ADD scripts/dub* /usr/local/lib/python2.7/dist-packages/confluent/docker_utils/
ADD config/kafka* /etc/kafka/
ADD libs/kafka* /usr/share/java/kafka/

