# Multi-stage build for API Gateway
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace/app

# Copy parent pom and modules
COPY pom.xml .
COPY common ./common
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

