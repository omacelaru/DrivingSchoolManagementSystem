# Driving School Management System

A modern microservices-based driving school management platform built with Spring Boot 4.0.1, featuring API Gateway, Kafka, Redis, and multi-database support.

## Architecture

This project follows a microservices architecture pattern with the following services:

- **API Gateway** - Single entry point for all client requests
- **Student Service** - Student registration and management
- **Scheduling Service** - Lesson booking and scheduling
- **Vehicle Service** - Vehicle fleet management
- **Payment Service** - Payment processing and invoicing
- **Notification Service** - Event-driven notifications

## Technology Stack

- **Java 23**
- **Spring Boot 4.0.1**
- **Spring Cloud 2023.0.0**
- **Spring Cloud Gateway** - API Gateway
- **Apache Kafka** - Message broker
- **Redis** - Caching
- **PostgreSQL** - Primary database
- **MySQL** - Replica database (failover)
- **SpringDoc OpenAPI** - API documentation (Swagger)
- **JUnit 5 & Mockito** - Testing
- **Testcontainers** - Integration testing

## Prerequisites

- Java 23 or higher
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15+
- MySQL 8+
- Redis 7+
- Apache Kafka 3.5+

## Quick Start

### 1. Start Infrastructure Services

```bash
docker-compose up -d
```

This will start:
- PostgreSQL (port 5432)
- MySQL (port 3306)
- Redis (port 6379)
- Kafka & Zookeeper (ports 9092, 2181)

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run Services

Start services in order:
1. Common module (build only)
2. Student Service
3. Scheduling Service
4. Vehicle Service
5. Payment Service
6. Notification Service
7. API Gateway

Or use the provided scripts:
```bash
./scripts/start-all.sh
```

### 4. Access Services

- **API Gateway:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Student Service:** http://localhost:8081
- **Scheduling Service:** http://localhost:8082
- **Vehicle Service:** http://localhost:8083
- **Payment Service:** http://localhost:8084
- **Notification Service:** http://localhost:8085

## API Documentation

All APIs are documented using Swagger/OpenAPI 3.0. Access the interactive documentation at:
- **API Gateway Swagger:** http://localhost:8080/swagger-ui.html

## Testing

Run all tests:
```bash
mvn test
```

Run tests with coverage:
```bash
mvn test jacoco:report
```

## Project Structure

```
driving-school-platform/
├── api-gateway/          # API Gateway service
├── student-service/      # Student management
├── scheduling-service/   # Lesson scheduling
├── vehicle-service/      # Vehicle management
├── payment-service/      # Payment processing
├── notification-service/ # Notifications
├── common/              # Shared code
└── docker-compose.yml   # Infrastructure setup
```

## Features

### MVP Features Implemented

1. **Student Management** - Complete student lifecycle
2. **Instructor & Lesson Scheduling** - Efficient booking system
3. **Vehicle Management** - Fleet tracking and maintenance
4. **Payment Processing** - Financial transactions
5. **Exam Management** - Exam scheduling and results

## Database Schema

The system uses 8+ entities with multiple relationships:
- Student, Instructor, Vehicle, Lesson, Payment, Exam, Course, Document

See `REQUIREMENTS.md` for detailed entity relationships.

## Postman Collection

Import the Postman collection from `postman/` directory to test all APIs.

## Contributing

1. Follow Java coding conventions
2. Write unit tests for all new features
3. Maintain code coverage above 80%
4. Update Swagger documentation
5. Follow DRY principles

## License

This project is for educational purposes.

