FROM java:8-alpine

WORKDIR /app

RUN apk add --update \
    curl \
    && rm -rf /var/cache/apk/*

RUN curl https://repo1.maven.org/maven2/io/hawt/hawtio-app/2.8.0/hawtio-app-2.8.0.jar -o /app/hawtio-app-2.8.0.jar

CMD [ "java", "-Dhawtio.proxyWhitelist=*", "-jar", "/app/hawtio-app-2.8.0.jar" ]