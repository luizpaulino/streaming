FROM amazoncorretto:17-alpine-jdk
LABEL authors="Luiz"

COPY target/streaming-0.0.1-SNAPSHOT.jar streaming.jar
COPY application.yml application.yml
ENTRYPOINT ["java","-jar","/streaming.jar"]