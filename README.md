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
- **Spring Cloud 2025.1.0**
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
- PostgreSQL 17+
- MySQL 9+
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
- **Individual Service Swagger UIs:**
  - Student Service: http://localhost:8081/swagger-ui.html
  - Scheduling Service: http://localhost:8082/swagger-ui.html
  - Vehicle Service: http://localhost:8083/swagger-ui.html
  - Payment Service: http://localhost:8084/swagger-ui.html
  - Instructor Service: http://localhost:8086/swagger-ui.html

### Combined OpenAPI Documentation

To generate a single OpenAPI file containing all services' APIs:

1. **Start all services** (or at least the ones you want to include)

2. **Run the aggregation script:**
   ```powershell
   cd scripts
   .\generate-combined-openapi.ps1
   ```

3. **The script will:**
   - Fetch OpenAPI specs from all running services
   - Merge them into a single `combined-openapi.json` file
   - Update all paths to use the API Gateway base URL
   - Save the file in the project root

4. **Use the combined file:**
   - **Swagger Editor:** Upload to https://editor.swagger.io/
   - **Postman:** Import -> File -> Select `combined-openapi.json`
   - **Share with team:** Single file contains all API documentation

**Script options:**
```powershell
# Custom output file name
.\generate-combined-openapi.ps1 -OutputFile "all-apis.json"

# Custom base URL (if services run on different host)
.\generate-combined-openapi.ps1 -BaseUrl "http://192.168.1.100"
```

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

Import the Postman collection `DrivingSchool_API.postman_collection.json` to test all APIs. The collection includes:
- All endpoints organized by service
- Example requests with test data
- Environment variables for easy configuration
- Complete API coverage

### Automatic Postman Collection Update (from OpenAPI)

You can automatically update the Postman collection from the live OpenAPI definition exposed by the API Gateway (or any service).

1. **Start the services** (at least the API Gateway) so that OpenAPI is available at:
   - `http://localhost:8080/v3/api-docs`

2. **Set Postman credentials** (once per machine):
   - `POSTMAN_API_KEY` – your Postman API key
   - `POSTMAN_COLLECTION_UID` – UID of the `DrivingSchool_API` collection in Postman

3. **Run the update script (PowerShell on Windows):**

```powershell
cd path\to\DrivingSchoolManagementSystem
.\scripts\update-postman-from-openapi.ps1 -OpenApiUrl "http://localhost:8080/v3/api-docs"
```

The script will:
- Fetch the OpenAPI JSON from the given URL
- Send it to the Postman Import API to generate a collection
- Overwrite the existing Postman collection with the new definition (same UID)

You can also adjust `-OpenApiUrl` to point directly to a specific microservice (e.g. `http://localhost:8084/v3/api-docs` for the Payment Service) if you prefer per-service collections.

See `API_USAGE_GUIDE.md` for detailed usage instructions and examples.

## Contributing

1. Follow Java coding conventions
2. Write unit tests for all new features
3. Maintain code coverage above 80%
4. Update Swagger documentation
5. Follow DRY principles

## License

This project is for educational purposes.

