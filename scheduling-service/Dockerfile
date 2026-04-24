# Multi-stage build for Scheduling Service
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace/app

# Copy parent pom and modules
COPY pom.xml .
COPY common ./common
COPY scheduling-service ./scheduling-service

# Build only scheduling-service module
RUN mvn clean package -am -pl scheduling-service -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DSMS Team"
LABEL description="Driving School Management System - Scheduling Service"

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /workspace/app/scheduling-service/target/scheduling-service-*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Expose port
EXPOSE 8082

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

