# Stage 1: Build stage (Maven + JDK 21)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 1. Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# 2. Copy source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage (JRE 21 only)
FROM eclipse-temurin:21-jre-alpine
LABEL authors="Lalit"
WORKDIR /app

# 3. Copy the jar from the build stage
# Matches <artifactId>community-service</artifactId> from your pom.xml
COPY --from=build /app/target/community-service-0.0.1-SNAPSHOT.jar app.jar

# Set the port to 8090 as requested
EXPOSE 8090

# 4. Run the application
# Using --server.port=8090 ensures the internal port matches your requirement
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8090"]