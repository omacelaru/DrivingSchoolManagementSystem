package com.drivingschool.student.controller;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.student.dto.DocumentResponse;
import com.drivingschool.student.dto.DocumentUpdateRequest;
import com.drivingschool.student.dto.StudentRequest;
import com.drivingschool.student.dto.StudentResponse;
import com.drivingschool.student.entity.Document;
import com.drivingschool.student.entity.Student;
import com.drivingschool.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Student Management",
        description = "Students: CRUD, search, and documents under /{id}/documents (upload, list, PUT metadata, DELETE).")
public class StudentController {
    private final StudentService studentService;

    @PostMapping
    @Operation(summary = "Register a new student", 
              description = "Creates a new student with the provided information. Validates CNP, email, and phone number.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Student created successfully",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation failed"),
        @ApiResponse(responseCode = "409", description = "Student with this CNP or email already exists")
    })
    public ResponseEntity<ApiResult<StudentResponse>> createStudent(
            @Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("Student registered successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by ID", 
              description = "Retrieves detailed information about a student by their unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student found",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<ApiResult<StudentResponse>> getStudent(
            @Parameter(description = "Unique student identifier", example = "1", required = true) 
            @PathVariable Long id) {
        StudentResponse response = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student information", 
              description = "Updates existing student details. All fields are validated.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student updated successfully",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<ApiResult<StudentResponse>> updateStudent(
            @Parameter(description = "Unique student identifier", example = "1", required = true) 
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResult.success("Student updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student", 
              description = "Permanently deletes a student from the system. This action cannot be undone.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Student not found"),
        @ApiResponse(responseCode = "409", description = "Cannot delete student with active lessons or payments")
    })
    public ResponseEntity<ApiResult<Void>> deleteStudent(
            @Parameter(description = "Unique student identifier", example = "1", required = true) 
            @PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResult.success("Student deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get all students", 
              description = "Retrieves a list of all students. Can be optionally filtered by status (ACTIVE, INACTIVE, SUSPENDED).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of students retrieved successfully")
    })
    public ResponseEntity<ApiResult<List<StudentResponse>>> getAllStudents(
            @Parameter(description = "Filter by student status (ACTIVE, INACTIVE, SUSPENDED)", example = "ACTIVE") 
            @RequestParam(required = false) Student.StudentStatus status) {
        List<StudentResponse> students = studentService.getAllStudents(status);
        return ResponseEntity.ok(ApiResult.success(students));
    }

    @GetMapping("/search")
    @Operation(summary = "Search students by name", 
              description = "Searches for students by first name or last name. The search is case-insensitive and partial matches are supported.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<ApiResult<List<StudentResponse>>> searchStudents(
            @Parameter(description = "Name to search (first or last name)", example = "John", required = true) 
            @RequestParam String name) {
        List<StudentResponse> students = studentService.searchStudentsByName(name);
        return ResponseEntity.ok(ApiResult.success(students));
    }

    @PostMapping("/{id}/documents")
    @Operation(summary = "Upload student document",
              description = "Uploads a document for a student. Types: ID_COPY, PHOTO, MEDICAL_CERTIFICATE, DRIVING_LICENSE_COPY.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Document uploaded successfully",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid document type or file path"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<ApiResult<DocumentResponse>> uploadDocument(
            @Parameter(description = "Unique student identifier", example = "1", required = true) 
            @PathVariable Long id,
            @Parameter(description = "Document type", example = "ID_COPY", required = true)
            @RequestParam Document.DocumentType documentType,
            @Parameter(description = "Path to the document file", example = "/documents/student_1/id_card.pdf", required = true) 
            @RequestParam String filePath) {
        DocumentResponse response = studentService.uploadDocument(id, documentType, filePath);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("Document uploaded successfully", response));
    }

    @GetMapping("/{id}/documents")
    @Operation(summary = "Get student documents", 
              description = "Retrieves all documents associated with a specific student, including their status and upload dates.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<ApiResult<List<DocumentResponse>>> getStudentDocuments(
            @Parameter(description = "Unique student identifier", example = "1", required = true) 
            @PathVariable Long id) {
        List<DocumentResponse> documents = studentService.getStudentDocuments(id);
        return ResponseEntity.ok(ApiResult.success(documents));
    }

    @PutMapping("/{id}/documents/{documentId}")
    @Operation(summary = "Update student document metadata",
              description = "Updates document type, file path, and/or status. Only sent fields are changed.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document updated",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
        @ApiResponse(responseCode = "400", description = "No fields to update or invalid file path"),
        @ApiResponse(responseCode = "404", description = "Student or document not found")
    })
    public ResponseEntity<ApiResult<DocumentResponse>> updateStudentDocument(
            @Parameter(description = "Student id", required = true) @PathVariable Long id,
            @Parameter(description = "Document id", required = true) @PathVariable Long documentId,
            @Valid @RequestBody DocumentUpdateRequest request) {
        DocumentResponse response = studentService.updateStudentDocument(id, documentId, request);
        return ResponseEntity.ok(ApiResult.success("Document updated successfully", response));
    }

    @DeleteMapping("/{id}/documents/{documentId}")
    @Operation(summary = "Delete student document",
              description = "Removes the document record. Does not delete physical files on disk.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Document deleted"),
        @ApiResponse(responseCode = "404", description = "Student or document not found")
    })
    public ResponseEntity<ApiResult<Void>> deleteStudentDocument(
            @Parameter(description = "Student id", required = true) @PathVariable Long id,
            @Parameter(description = "Document id", required = true) @PathVariable Long documentId) {
        studentService.deleteStudentDocument(id, documentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResult.success("Document deleted successfully", null));
    }
}

