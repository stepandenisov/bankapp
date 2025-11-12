FROM eclipse-temurin:21-jdk

WORKDIR /app

ARG JAR_FILE=transfer/target/*.jar
COPY ${JAR_FILE} app.jar

COPY wait-for-config-server.sh ./wait-for-config-server.sh

ENV JAVA_OPTS=""

ENTRYPOINT ["./wait-for-config-server.sh"]
