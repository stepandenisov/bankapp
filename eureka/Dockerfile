#FROM maven:3.9.8-eclipse-temurin-21 AS builder
#
#WORKDIR /app
#
#COPY pom.xml ./pom.xml
#
#COPY settings.xml /root/.m2/settings.xml
#
#ARG MODULE=eureka
#COPY ${MODULE}/pom.xml ./${MODULE}/pom.xml
#COPY ${MODULE}/src ./${MODULE}/src
#
#RUN rm -rf /root/.m2/repository/org/apache/activemq/artemis-bom \
#           /root/.m2/repository/org/apache/activemq/artemis-project
#
#RUN mvn clean install -N -U
#
#RUN mvn -f ${MODULE}/pom.xml clean package
#
#
#FROM eclipse-temurin:21-jdk
#WORKDIR /app
#
#ARG MODULE=eureka
#COPY --from=builder /app/${MODULE}/target/*.jar app.jar
#
#EXPOSE 8761
#
#ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jdk

WORKDIR /app

ARG JAR_FILE=eureka/target/*.jar
COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]