# Stage 1: build JAR
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: run app
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/kuzahealth-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
