# Stage 1: build JAR
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn -B -DskipTests clean package

# Stage 2: run app
FROM openjdk:17-jdk-slim
WORKDIR /app
# Copy any built jar as app.jar (avoids hardcoding version)
COPY --from=builder /app/target/*.jar /app/app.jar

# Environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Render will set PORT; app.properties already maps server.port to PORT
EXPOSE 8080

# Use sh -c so JAVA_OPTS can be expanded; keep PID 1 as java process
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
