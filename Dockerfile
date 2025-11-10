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

# 1. Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# 2. Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# 3. FIX: Create the upload directory and change its ownership to 'spring'.
# This ensures the non-root user can write files here.
RUN mkdir -p /app/file-uploads && chown spring:spring /app/file-uploads

# 4. Switch to the non-root user
USER spring:spring

# Expose port (change if needed)
EXPOSE 8080

# Default command
ENTRYPOINT ["java","-jar","app.jar"]