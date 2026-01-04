package com.drivingschool.student.dto;

import com.drivingschool.student.entity.Student;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Response DTO containing student information")
public record StudentResponse(
    @Schema(description = "Unique student identifier", example = "1")
    Long id,
    
    @Schema(description = "Student's first name", example = "John")
    String firstName,
    
    @Schema(description = "Student's last name", example = "Doe")
    String lastName,
    
    @Schema(description = "Romanian CNP (Personal Numeric Code)", example = "1234567890123")
    String cnp,
    
    @Schema(description = "Student's email address", example = "john.doe@example.com")
    String email,
    
    @Schema(description = "Student's phone number", example = "0123456789")
    String phone,
    
    @Schema(description = "Student's address", example = "123 Main Street, Bucharest")
    String address,
    
    @Schema(description = "Student registration status", example = "ACTIVE")
    Student.StudentStatus status,
    
    @Schema(description = "Date and time when student was registered", example = "2027-01-01T10:30:00")
    LocalDateTime registrationDate,
    
    @Schema(description = "Date and time when student information was last modified", example = "2027-01-01T14:45:00")
    LocalDateTime lastModifiedDate,
    
    @Schema(description = "List of documents associated with the student")
    List<DocumentResponse> documents
) {
}

