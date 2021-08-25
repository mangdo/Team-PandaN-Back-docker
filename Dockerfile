FROM openjdk:8-jdk-alpine

ARG JAR_FILE=build/libs/Team-PandaN-Back-0.0.1-SNAPSHOT.jar
ARG PROPERTIES_FILE=src/main/resources/application.properties

COPY ${JAR_FILE} app.jar
COPY ${PROPERTIES_FILE} application-prod.properties

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod","-Duser.timezone=Asia/Seoul", "/app.jar"]