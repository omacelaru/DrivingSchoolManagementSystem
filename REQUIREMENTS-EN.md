# Business Requirements - Driving School Management System

## 1. Business Requirements

### BR1: Student Management
The system allows registration, updating and deletion of students. Each student has a unique CNP, unique email, valid phone (10 digits) and address. Available status: PENDING, ACTIVE, SUSPENDED, GRADUATED.

### BR2: Student Document Management
The system allows uploading and managing documents for each student. Accepted types: ID copy, medical certificate, photograph, license copy. Document status: PENDING, APPROVED, REJECTED.

### BR3: Instructor Management
The system allows registration and management of instructors with unique license number, unique email, valid phone and specialization (THEORETICAL, PRACTICAL, BOTH). Includes search by specialization and availability verification.

### BR4: Vehicle Management
The system allows registration and management of the vehicle fleet with unique registration number, brand, model, year, insurance expiration date and status (AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_SERVICE). Includes sending to maintenance and returning to service.

### BR5: Vehicle Maintenance Management
The system allows registration and tracking of maintenance operations for each vehicle. Each operation contains date, type (ROUTINE, REPAIR, INSPECTION, OTHER), description and cost.

### BR6: Course Management
The system allows creation and management of courses with name, description, price, associated instructor, associated vehicle, number of lessons and type (THEORETICAL, PRACTICAL). Deletion is only allowed if there are no associated lessons.

### BR7: Lesson Scheduling
The system allows booking, updating and canceling lessons with automatic verification of instructor and vehicle availability. Lesson status: SCHEDULED, COMPLETED, CANCELLED, NO_SHOW.

### BR8: Availability Verification
The system verifies the availability of instructors and vehicles for a specified time interval, taking into account all scheduled lessons to prevent overlapping.

### BR9: Payment Management
The system processes payments for lessons and courses. Each payment contains student, amount, method (CARD, CASH, BANK_TRANSFER, ONLINE), status (PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED), transaction date and unique transaction ID. Includes balance calculation and refund processing.

### BR10: Event Notifications
The system sends automated notifications for important events: lesson booking, lesson cancellation, payment confirmation, sending vehicle to maintenance. Notifications are managed through asynchronous messaging.

## 2. MVP Features

### Feature 1: Student and Document Management
Allows complete student registration with CNP, email and phone validation, plus document upload and management with approval status tracking.

**Endpoints:**
- POST `/api/students` - Student registration
- GET `/api/students/{id}` - Student details
- PUT `/api/students/{id}` - Student update
- DELETE `/api/students/{id}` - Student deletion
- POST `/api/students/{id}/documents` - Document upload
- GET `/api/students/{id}/documents` - List documents

**Validations:**
- Unique and valid CNP
- Unique email
- 10-digit phone
- Document status: PENDING → APPROVED/REJECTED

### Feature 2: Lesson Scheduling and Management
Allows lesson booking with automatic instructor and vehicle availability verification. Prevents booking overlaps and allows rescheduling or cancellation.

**Endpoints:**
- POST `/api/lessons` - Lesson booking
- GET `/api/lessons/{id}` - Lesson details
- PUT `/api/lessons/{id}` - Lesson rescheduling
- DELETE `/api/lessons/{id}` - Lesson cancellation
- GET `/api/lessons/instructors/{instructorId}/availability` - Instructor availability verification
- GET `/api/lessons/vehicles/{vehicleId}/availability` - Vehicle availability verification
- GET `/api/lessons/students/{studentId}` - List student lessons

**Validations:**
- Booking conflict verification
- Time interval validation (startTime < endTime)
- Prevention of past bookings

### Feature 3: Course and Instructor Management
Allows creation and management of courses with associated instructor and vehicle, plus instructor search by specialization and availability verification.

**Endpoints:**
- POST `/api/courses` - Course creation
- GET `/api/courses/{id}` - Course details
- PUT `/api/courses/{id}` - Course update
- DELETE `/api/courses/{id}` - Course deletion
- POST `/api/instructors` - Instructor registration
- GET `/api/instructors/{id}` - Instructor details
- GET `/api/instructors/available` - List available instructors
- GET `/api/instructors/specialization/{specialization}` - Search by specialization

**Validations:**
- Unique instructor license number
- Unique instructor email
- Specialization: THEORETICAL, PRACTICAL, BOTH
- Course type: THEORETICAL, PRACTICAL
- Positive price and number of lessons

### Feature 4: Vehicle and Maintenance Management
Allows management of the vehicle fleet, including sending to maintenance and returning to service. Records all maintenance operations with costs and types.

**Endpoints:**
- POST `/api/vehicles` - Vehicle registration
- GET `/api/vehicles/{id}` - Vehicle details
- PUT `/api/vehicles/{id}` - Vehicle update
- GET `/api/vehicles/available` - List available vehicles
- PUT `/api/vehicles/{id}/maintenance` - Send to maintenance
- PUT `/api/vehicles/{id}/maintenance/return` - Return from maintenance

**Validations:**
- Unique registration number
- Future insurance expiration date
- Status: AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_SERVICE
- Maintenance type: ROUTINE, REPAIR, INSPECTION, OTHER
- Prevention of using vehicle in maintenance

### Feature 5: Payment Processing and Balances
Allows processing payments for lessons and courses with support for multiple payment methods. Calculates student balances and allows refunds.

**Endpoints:**
- PUT `/api/payments` - Payment processing
- POST `/api/payments/pending` - Create pending payment
- GET `/api/payments/{id}` - Payment details
- GET `/api/payments/student/{studentId}` - List student payments
- GET `/api/payments/student/{studentId}/balance` - Calculate student balance
- PUT `/api/payments/{id}/refund` - Process refund
- PUT `/api/payments/{id}/status` - Update payment status

**Validations:**
- Positive amount
- Method: CARD, CASH, BANK_TRANSFER, ONLINE
- Status: PENDING → COMPLETED/FAILED → REFUNDED
- Unique transaction ID
- Balance calculation = total amount of COMPLETED payments
- Prevention of double refund (pessimistic locking)

## 3. Entity Relationship Diagram (Database Schema)

The Entity-Relationship Diagram (ERD) of the system reflects the actual structure of the PostgreSQL database.

![ERD Diagram](DB-diagram.png)

### 4. Technical Notes

- **Audit Fields**: All entities contain audit fields (`created_at`, `last_modified_date`) for automatic tracking
- **Optimistic Locking**: The `courses` entity uses `version` for concurrency prevention
- **Cascade Operations**: JPA relationships are configured with cascade operations where necessary (e.g., Student → Documents)
- **Nullable Foreign Keys**: 
  - `lessons.course_id` is nullable to allow standalone lessons
  - `payments.lesson_id` is nullable for general payments (not associated with a specific lesson)
- **Field-Level Validations**: All entities have validations implemented through Bean Validation (@NotNull, @NotBlank, @Email, @Pattern, etc.)
- **Microservices Architecture**: Some relationships are logical (through IDs) between different services, not direct JPA relationships

