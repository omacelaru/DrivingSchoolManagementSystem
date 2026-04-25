# Multi-stage build for API Gateway
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace/app

# Copy parent pom and modules
COPY pom.xml .
COPY common ./common
COPY student-service ./student-service
COPY scheduling-service ./scheduling-service
COPY instructor-service ./instructor-service
COPY vehicle-service ./vehicle-service
COPY payment-service ./payment-service
COPY notification-service ./notification-service
COPY api-gateway ./api-gateway

# Build only api-gateway module
RUN mvn clean package -am -pl api-gateway -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DSMS Team"
LABEL description="Driving School Management System - API Gateway"

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /workspace/app/api-gateway/target/api-gateway-*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

