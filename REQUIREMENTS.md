# Driving School Management System - Requirements Document

## Business Domain: Driving School Management System

### 10 Business Requirements

1. **Student Registration & Management**
   - The system must allow new students to register with personal information (name, CNP, address, phone, email)
   - Students must be able to upload required documents (ID copy, medical certificate, photos)
   - The system should track student status (pending, active, suspended, graduated)

2. **Instructor Management**
   - The system must manage instructor profiles with qualifications, certifications, and availability
   - Track instructor ratings and performance metrics
   - Assign instructors to students based on availability and specialization

3. **Course & Lesson Scheduling**
   - Students must be able to book driving lessons with available instructors
   - The system should prevent double-booking and manage instructor schedules
   - Support for both theoretical and practical lessons
   - Automatic scheduling conflict detection

4. **Vehicle Management**
   - Track all vehicles in the fleet (license plate, model, year, insurance status)
   - Monitor vehicle availability and maintenance schedules
   - Assign vehicles to lessons automatically or manually

5. **Payment Processing**
   - Students must be able to make payments for courses and lessons
   - Track payment history and outstanding balances
   - Support multiple payment methods (card, cash, bank transfer)
   - Generate invoices and receipts automatically

6. **Exam Management**
   - Schedule theoretical and practical exams
   - Track exam results and student progress
   - Manage examiners and exam locations
   - Generate exam certificates upon successful completion

7. **Progress Tracking**
   - Monitor student progress through theoretical and practical training
   - Track completed lessons, hours, and mandatory requirements
   - Generate progress reports for students and administrators

8. **Notification System**
   - Send automated notifications for lesson reminders, exam dates, payment due dates
   - Support multiple channels (email, SMS, in-app notifications)
   - Customizable notification preferences per user

9. **Reporting & Analytics**
   - Generate reports on student enrollment, completion rates, revenue
   - Track instructor performance and vehicle utilization
   - Financial reports and analytics dashboard

10. **Multi-tenancy & Role-Based Access**
    - Support multiple driving school branches
    - Role-based access control (Admin, Instructor, Student, Receptionist)
    - Secure authentication and authorization

---

## MVP Features (Minimum Viable Product)

### Feature 1: Student Management
**Description:** Complete student lifecycle management from registration to graduation.

**Key Functionalities:**
- Student registration with document upload
- Student profile management (view, update, deactivate)
- Student status tracking (pending approval, active, suspended, graduated)
- Document validation and verification workflow

**Endpoints:**
- `POST /api/students` - Register new student
- `GET /api/students/{id}` - Get student details
- `PUT /api/students/{id}` - Update student information
- `GET /api/students` - List all students with filtering
- `POST /api/students/{id}/documents` - Upload student documents

**Entities:** Student, Document

---

### Feature 2: Instructor & Lesson Scheduling
**Description:** Manage instructors and schedule driving lessons efficiently.

**Key Functionalities:**
- Instructor profile management
- Lesson booking with availability checking
- Automatic conflict detection
- Instructor schedule view and management

**Endpoints:**
- `POST /api/instructors` - Register new instructor
- `GET /api/instructors/{id}/availability` - Check instructor availability
- `POST /api/lessons` - Book a new lesson
- `GET /api/lessons/{id}` - Get lesson details
- `PUT /api/lessons/{id}` - Update lesson (reschedule/cancel)
- `GET /api/instructors/{id}/lessons` - Get instructor's scheduled lessons

**Entities:** Instructor, Lesson, Schedule

---

### Feature 3: Vehicle Management
**Description:** Track and manage the driving school vehicle fleet.

**Key Functionalities:**
- Vehicle registration and information management
- Vehicle availability tracking
- Maintenance scheduling and reminders
- Vehicle assignment to lessons

**Endpoints:**
- `POST /api/vehicles` - Register new vehicle
- `GET /api/vehicles` - List all vehicles
- `GET /api/vehicles/{id}` - Get vehicle details
- `PUT /api/vehicles/{id}` - Update vehicle information
- `GET /api/vehicles/available` - Get available vehicles for a time slot

**Entities:** Vehicle, Maintenance

---

### Feature 4: Payment Processing
**Description:** Handle all financial transactions and payment tracking.

**Key Functionalities:**
- Process payments for courses and lessons
- Track payment history and outstanding balances
- Generate invoices and receipts
- Payment method management

**Endpoints:**
- `POST /api/payments` - Process a new payment
- `GET /api/payments/student/{studentId}` - Get student payment history
- `GET /api/payments/{id}` - Get payment details
- `POST /api/payments/{id}/invoice` - Generate invoice
- `GET /api/students/{id}/balance` - Get student account balance

**Entities:** Payment, Invoice, Course

---

### Feature 5: Exam Management
**Description:** Schedule and manage theoretical and practical exams.

**Key Functionalities:**
- Exam scheduling with examiner assignment
- Track exam results and student eligibility
- Manage exam locations and time slots
- Generate exam certificates

**Endpoints:**
- `POST /api/exams` - Schedule a new exam
- `GET /api/exams/{id}` - Get exam details
- `PUT /api/exams/{id}/results` - Submit exam results
- `GET /api/exams/student/{studentId}` - Get student exam history
- `GET /api/exams/upcoming` - Get upcoming exams

**Entities:** Exam, Examiner, ExamResult

---

## System Architecture

### Microservices Structure

1. **API Gateway Service** - Spring Cloud Gateway
   - Single entry point for all client requests
   - Routing, load balancing, authentication
   - Rate limiting and circuit breakers

2. **Student Service** - Student management
   - Database: PostgreSQL (primary), MySQL (replica)
   - Cache: Redis for student profiles

3. **Scheduling Service** - Lessons and scheduling
   - Database: PostgreSQL (primary), MySQL (replica)
   - Cache: Redis for availability slots
   - Kafka: Event publishing for lesson bookings

4. **Vehicle Service** - Vehicle management
   - Database: PostgreSQL (primary), MySQL (replica)
   - Cache: Redis for vehicle availability

5. **Payment Service** - Payment processing
   - Database: PostgreSQL (primary), MySQL (replica)
   - Kafka: Payment events
   - Redis: Transaction cache

6. **Notification Service** - Notifications
   - Kafka: Consumes events from other services
   - Sends emails, SMS, in-app notifications

### Technology Stack

- **Framework:** Spring Boot 4.0.1
- **API Gateway:** Spring Cloud Gateway
- **Service Discovery:** Spring Cloud Eureka (or Consul)
- **Message Broker:** Apache Kafka
- **Cache:** Redis
- **Databases:** PostgreSQL (primary), MySQL (replica for failover)
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **Testing:** JUnit 5, Mockito, Testcontainers
- **Build Tool:** Maven
- **Java Version:** 23

### Database Entities & Relations

1. **Student** (id, firstName, lastName, cnp, email, phone, address, status, registrationDate)
2. **Instructor** (id, firstName, lastName, licenseNumber, email, phone, specialization, rating)
3. **Vehicle** (id, licensePlate, make, model, year, insuranceExpiry, status)
4. **Lesson** (id, studentId, instructorId, vehicleId, startTime, endTime, type, status)
5. **Payment** (id, studentId, amount, paymentMethod, status, transactionDate, invoiceId)
6. **Exam** (id, studentId, examinerId, examType, scheduledDate, location, status, result)
7. **Course** (id, name, description, price, duration, category)
8. **Document** (id, studentId, documentType, filePath, uploadDate, status)

**Relations:**
- Student → Lesson (One-to-Many)
- Instructor → Lesson (One-to-Many)
- Vehicle → Lesson (One-to-Many)
- Student → Payment (One-to-Many)
- Student → Exam (One-to-Many)
- Student → Document (One-to-Many)
- Course → Payment (Many-to-One)

---

## API Documentation

All APIs will be documented using Swagger/OpenAPI 3.0 and accessible at:
- API Gateway: `http://localhost:8080/swagger-ui.html`
- Individual Services: `http://localhost:{port}/swagger-ui.html`

---

## Testing Strategy

- **Unit Tests:** All services and business logic
- **Integration Tests:** All REST endpoints
- **Test Coverage:** Minimum 80% code coverage
- **Test Containers:** For database and infrastructure testing

---

## Deployment & Demonstration

- **Postman Collection:** Complete API collection for testing
- **Docker Compose:** For local development with all services
- **Environment:** Development, Staging, Production configurations

