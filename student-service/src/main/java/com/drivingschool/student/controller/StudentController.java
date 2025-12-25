package com.drivingschool.student.controller;

import com.drivingschool.common.dto.ApiResponse;
import com.drivingschool.student.dto.DocumentResponse;
import com.drivingschool.student.dto.StudentRequest;
import com.drivingschool.student.dto.StudentResponse;
import com.drivingschool.student.entity.Document;
import com.drivingschool.student.entity.Student;
import com.drivingschool.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Management", description = "APIs for managing students")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    @Operation(summary = "Register a new student", description = "Creates a new student with the provided information")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student registered successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by ID", description = "Retrieves student details by student ID")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudent(
            @Parameter(description = "Student ID") @PathVariable Long id) {
        StudentResponse response = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student information", description = "Updates student details")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @Parameter(description = "Student ID") @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Student updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student", description = "Deletes a student by ID")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @Parameter(description = "Student ID") @PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get all students", description = "Retrieves all students, optionally filtered by status")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllStudents(
            @Parameter(description = "Filter by status") @RequestParam(required = false) Student.StudentStatus status) {
        List<StudentResponse> students = studentService.getAllStudents(status);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @GetMapping("/search")
    @Operation(summary = "Search students by name", description = "Searches students by first or last name")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> searchStudents(
            @Parameter(description = "Name to search") @RequestParam String name) {
        List<StudentResponse> students = studentService.searchStudentsByName(name);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @PostMapping("/{id}/documents")
    @Operation(summary = "Upload student document", description = "Uploads a document for a student")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @Parameter(description = "Student ID") @PathVariable Long id,
            @Parameter(description = "Document type") @RequestParam Document.DocumentType documentType,
            @Parameter(description = "File path") @RequestParam String filePath) {
        DocumentResponse response = studentService.uploadDocument(id, documentType, filePath);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully", response));
    }

    @GetMapping("/{id}/documents")
    @Operation(summary = "Get student documents", description = "Retrieves all documents for a student")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getStudentDocuments(
            @Parameter(description = "Student ID") @PathVariable Long id) {
        List<DocumentResponse> documents = studentService.getStudentDocuments(id);
        return ResponseEntity.ok(ApiResponse.success(documents));
    }
}

