# Multi-stage build for Vehicle Service
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace/app

# Copy parent pom and modules
COPY pom.xml .
COPY common ./common
COPY vehicle-service ./vehicle-service

# Build only vehicle-service module
RUN mvn clean package -am -pl vehicle-service -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DSMS Team"
LABEL description="Driving School Management System - Vehicle Service"

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /workspace/app/vehicle-service/target/vehicle-service-*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Expose port
EXPOSE 8083

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

