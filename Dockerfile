# Multi-stage build for optimized production image
FROM maven:3.8.6-openjdk-8-slim as builder

# Set working directory
WORKDIR /app

# Copy pom.xml first to leverage Docker cache
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:8-jre-alpine

# Install curl for health checks
RUN apk --no-cache add curl

# Create non-root user for security
RUN addgroup -g 1001 -S appuser && \
    adduser -S appuser -u 1001 -G appuser

# Set working directory
WORKDIR /app

# Copy jar file from builder stage
COPY --from=builder /app/target/springboot-starterkit-1.0.jar app.jar

# Copy SSL certificates if needed
COPY --from=builder /app/src/main/resources/ats.jks /app/ats.jks

# Create temp directory for file processing
RUN mkdir -p /tmp/ats-temp && \
    chown -R appuser:appuser /app /tmp/ats-temp

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]