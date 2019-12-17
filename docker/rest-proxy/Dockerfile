FROM confluentinc/cp-kafka-rest:5.3.1

RUN curl https://search.maven.org/remotecontent?filepath=org/jolokia/jolokia-jvm/1.6.2/jolokia-jvm-1.6.2-agent.jar -o /usr/share/java/kafka-rest/jolokia-jvm-1.6.2-agent.jar

ADD config/kafka* /etc/kafka-rest/
ADD libs/kafka* /usr/share/java/kafka-rest/
