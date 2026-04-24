# Multi-stage build for Payment Service
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace/app

# Copy parent pom and modules
COPY pom.xml .
COPY common ./common
COPY payment-service ./payment-service

# Build only payment-service module
RUN mvn clean package -am -pl payment-service -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DSMS Team"
LABEL description="Driving School Management System - Payment Service"

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /workspace/app/payment-service/target/payment-service-*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# Expose port
EXPOSE 8084

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

