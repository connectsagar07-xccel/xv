# ---------- Stage 1: Build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy only pom.xml first for caching dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the project
COPY src ./src

# Build the Spring Boot application (skip tests if needed)
RUN mvn clean package -DskipTests

# ---------- Stage 2: Run ----------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (change if needed)
EXPOSE 8080

# Default command
ENTRYPOINT ["java","-jar","app.jar"]
