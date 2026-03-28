FROM eclipse-temurin:17-jdk-alpine

# Install Chrome and ChromeDriver
RUN apk add --no-cache \
    chromium \
    chromium-chromedriver

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests

# Run application
EXPOSE 8080
CMD ["java", "-jar", "target/ai-job-applier-1.0.0.jar"]
