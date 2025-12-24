# Setup Instructions - Driving School Management System

## Prerequisites

- Java 23 or higher
- Maven 3.8+
- Docker & Docker Compose
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Step-by-Step Setup

### 1. Start Infrastructure Services

```bash
# Navigate to project root
cd /home/macealaru/Documents/BDTS-AN1/Sem1/java/Project

# Start all infrastructure services (PostgreSQL, MySQL, Redis, Kafka)
docker-compose up -d

# Verify services are running
docker-compose ps
```

### 2. Build the Project

```bash
# Build all modules
mvn clean install

# If you encounter issues, build without tests first
mvn clean install -DskipTests
```

### 3. Configure Databases

The services are pre-configured to use:
- **PostgreSQL**: `localhost:5432` (database: `drivingschool`, user: `postgres`, password: `postgres`)
- **MySQL**: `localhost:3306` (database: `drivingschool_replica`, user: `root`, password: `root`)

Databases will be created automatically on first run.

### 4. Run Services

You can run services in any order, but recommended order:

#### Option A: Run from IDE
1. Open each service's main class
2. Run `*ServiceApplication.java` for each service:
   - `StudentServiceApplication` (port 8081)
   - `SchedulingServiceApplication` (port 8082)
   - `VehicleServiceApplication` (port 8083)
   - `PaymentServiceApplication` (port 8084)
   - `NotificationServiceApplication` (port 8085)
   - `ApiGatewayApplication` (port 8080)

#### Option B: Run from Command Line

```bash
# Terminal 1 - Student Service
cd student-service
mvn spring-boot:run

# Terminal 2 - Scheduling Service
cd scheduling-service
mvn spring-boot:run

# Terminal 3 - Vehicle Service
cd vehicle-service
mvn spring-boot:run

# Terminal 4 - Payment Service
cd payment-service
mvn spring-boot:run

# Terminal 5 - Notification Service
cd notification-service
mvn spring-boot:run

# Terminal 6 - API Gateway
cd api-gateway
mvn spring-boot:run
```

### 5. Verify Services

Check service health:
- Student Service: http://localhost:8081/actuator/health
- Scheduling Service: http://localhost:8082/actuator/health
- Vehicle Service: http://localhost:8083/actuator/health
- Payment Service: http://localhost:8084/actuator/health
- API Gateway: http://localhost:8080/actuator/health

### 6. Access Swagger Documentation

- **API Gateway Swagger**: http://localhost:8080/swagger-ui.html
- **Student Service Swagger**: http://localhost:8081/swagger-ui.html
- **Scheduling Service Swagger**: http://localhost:8082/swagger-ui.html
- **Vehicle Service Swagger**: http://localhost:8083/swagger-ui.html
- **Payment Service Swagger**: http://localhost:8084/swagger-ui.html

## Testing the APIs

### Using Swagger UI

1. Navigate to http://localhost:8080/swagger-ui.html
2. Select a service endpoint
3. Click "Try it out"
4. Fill in the request body
5. Click "Execute"

### Using cURL

#### Create a Student
```bash
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "cnp": "1234567890123",
    "email": "john.doe@example.com",
    "phone": "0123456789",
    "address": "123 Main St"
  }'
```

#### Book a Lesson
```bash
curl -X POST http://localhost:8080/api/lessons \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "instructorId": 1,
    "startTime": "2024-12-20T10:00:00",
    "endTime": "2024-12-20T11:00:00",
    "type": "PRACTICAL"
  }'
```

#### Register a Vehicle
```bash
curl -X POST http://localhost:8080/api/vehicles \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "AB-12-CDE",
    "make": "Toyota",
    "model": "Corolla",
    "year": 2020,
    "insuranceExpiry": "2025-12-31"
  }'
```

#### Process a Payment
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "amount": 1000.00,
    "paymentMethod": "CARD"
  }'
```

## Running Tests

```bash
# Run all tests
mvn test

# Run tests for a specific service
cd student-service
mvn test

# Run with coverage
mvn test jacoco:report
```

## Troubleshooting

### Port Already in Use
If a port is already in use, either:
1. Stop the service using that port
2. Change the port in `application.yml` of the service

### Database Connection Issues
1. Verify Docker containers are running: `docker-compose ps`
2. Check database logs: `docker-compose logs postgres` or `docker-compose logs mysql`
3. Verify connection strings in `application.yml`

### Kafka Connection Issues
1. Check Kafka is running: `docker-compose logs kafka`
2. Verify Kafka bootstrap server in `application.yml`: `localhost:9092`

### Redis Connection Issues
1. Check Redis is running: `docker-compose logs redis`
2. Verify Redis host/port in `application.yml`: `localhost:6379`

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
├── docker-compose.yml   # Infrastructure
├── pom.xml              # Parent POM
├── REQUIREMENTS.md      # Requirements document
├── PROJECT_SUMMARY.md   # Project summary
└── README.md            # Main README
```

## Next Steps

1. **Add Authentication**: Implement JWT-based authentication
2. **Add Service Discovery**: Integrate Eureka or Consul
3. **Add Circuit Breakers**: Implement Resilience4j
4. **Add Monitoring**: Integrate Prometheus and Grafana
5. **Complete Exam Service**: Implement the 5th MVP feature
6. **Add Integration Tests**: Use Testcontainers for full integration testing

## Support

For issues or questions, refer to:
- `REQUIREMENTS.md` - Business requirements
- `PROJECT_SUMMARY.md` - Architecture and compliance
- `README.md` - General project information

