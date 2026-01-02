package com.drivingschool.student.dto;

import com.drivingschool.student.entity.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing student information")
public class StudentResponse {
    @Schema(description = "Unique student identifier", example = "1")
    private Long id;
    
    @Schema(description = "Student's first name", example = "John")
    private String firstName;
    
    @Schema(description = "Student's last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "Romanian CNP (Personal Numeric Code)", example = "1234567890123")
    private String cnp;
    
    @Schema(description = "Student's email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Student's phone number", example = "0123456789")
    private String phone;
    
    @Schema(description = "Student's address", example = "123 Main Street, Bucharest")
    private String address;
    
    @Schema(description = "Student registration status", example = "ACTIVE")
    private Student.StudentStatus status;
    
    @Schema(description = "Date and time when student was registered", example = "2027-01-01T10:30:00")
    private LocalDateTime registrationDate;
    
    @Schema(description = "Date and time when student information was last modified", example = "2027-01-01T14:45:00")
    private LocalDateTime lastModifiedDate;
    
    @Schema(description = "List of documents associated with the student")
    private List<DocumentResponse> documents;
}

