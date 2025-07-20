# Stage 1: Build the JAR using Maven
FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy source code
COPY . .

# Build the JAR
RUN mvn clean package -DskipTests

# Stage 2: Use a lightweight JDK image
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=builder /app/target/kuzahealth-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
