package com.drivingschool.instructor.controller;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.instructor.dto.InstructorRequest;
import com.drivingschool.instructor.dto.InstructorResponse;
import com.drivingschool.instructor.entity.Instructor;
import com.drivingschool.instructor.service.InstructorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
@Tag(name = "Instructor Management", description = "APIs for managing instructors, including registration and retrieval")
public class InstructorController {
    private final InstructorService instructorService;

    @PostMapping
    @Operation(summary = "Register a new instructor", 
              description = "Creates a new instructor with the provided information. Validates license number and email uniqueness.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Instructor created successfully",
                    content = @Content(schema = @Schema(implementation = InstructorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation failed"),
        @ApiResponse(responseCode = "409", description = "Instructor with this license number or email already exists")
    })
    public ResponseEntity<ApiResult<InstructorResponse>> createInstructor(
            @Valid @RequestBody InstructorRequest request) {
        InstructorResponse response = instructorService.createInstructor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("Instructor registered successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get instructor by ID", 
              description = "Retrieves detailed information about a specific instructor.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Instructor found",
                    content = @Content(schema = @Schema(implementation = InstructorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Instructor not found")
    })
    public ResponseEntity<ApiResult<InstructorResponse>> getInstructor(
            @Parameter(description = "Unique instructor identifier", example = "1", required = true) 
            @PathVariable Long id) {
        InstructorResponse response = instructorService.getInstructorById(id);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all instructors", 
              description = "Retrieves a list of all instructors in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Instructors retrieved successfully")
    })
    public ResponseEntity<ApiResult<List<InstructorResponse>>> getAllInstructors() {
        List<InstructorResponse> instructors = instructorService.getAllInstructors();
        return ResponseEntity.ok(ApiResult.success(instructors));
    }

    @GetMapping("/specialization/{specialization}")
    @Operation(summary = "Get instructors by specialization", 
              description = "Retrieves all instructors with a specific specialization.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Instructors retrieved successfully")
    })
    public ResponseEntity<ApiResult<List<InstructorResponse>>> getInstructorsBySpecialization(
            @Parameter(description = "Instructor specialization", example = "BOTH", required = true) 
            @PathVariable Instructor.Specialization specialization) {
        List<InstructorResponse> instructors = instructorService.getInstructorsBySpecialization(specialization);
        return ResponseEntity.ok(ApiResult.success(instructors));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available instructors", 
              description = "Finds all instructors who are available for a specific time slot. Checks for existing lesson conflicts.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Available instructors retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date/time format")
    })
    public ResponseEntity<ApiResult<List<InstructorResponse>>> getAvailableInstructors(
            @Parameter(description = "Start date and time (ISO format)", example = "2027-01-01T10:00:00", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End date and time (ISO format)", example = "2027-01-01T11:00:00", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<InstructorResponse> instructors = instructorService.getAvailableInstructors(startTime, endTime);
        return ResponseEntity.ok(ApiResult.success(instructors));
    }
}

