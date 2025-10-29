FROM eclipse-temurin:21-jdk

WORKDIR /app

ARG JAR_FILE=config-server/target/*.jar
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
