FROM eclipse-temurin:21-jdk

WORKDIR /app

ARG JAR_FILE=cash/target/*.jar
COPY ${JAR_FILE} app.jar

COPY wait-for-config-server.sh ./wait-for-config-server.sh
RUN apt-get update && apt-get install -y dos2unix \
    && dos2unix ./wait-for-config-server.sh \
    && chmod +x ./wait-for-config-server.sh \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

ENV JAVA_OPTS=""

ENTRYPOINT ["./wait-for-config-server.sh"]
