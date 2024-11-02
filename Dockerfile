FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/kuzahealth-0.0.1-SNAPSHOT.jar /app/myapp.jar

EXPOSE 8080

CMD [ "java","-jar","myapp.jar" ]