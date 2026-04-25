# Multi-stage build for Notification Service
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace/app

# Copy parent pom and modules
COPY pom.xml .
COPY common ./common
COPY api-gateway ./api-gateway
COPY student-service ./student-service
COPY scheduling-service ./scheduling-service
COPY instructor-service ./instructor-service
COPY vehicle-service ./vehicle-service
COPY payment-service ./payment-service
COPY notification-service ./notification-service

# Build only notification-service module
RUN mvn clean package -am -pl notification-service -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DSMS Team"
LABEL description="Driving School Management System - Notification Service"

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /workspace/app/notification-service/target/notification-service-*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

# Expose port
EXPOSE 8085

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

