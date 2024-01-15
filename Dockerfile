FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ./target/mediaController-0.0.1-SNAPSHOT.jar mediaController.jar
VOLUME /home/alejo/Desktop/test/images
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/mediaController.jar"]