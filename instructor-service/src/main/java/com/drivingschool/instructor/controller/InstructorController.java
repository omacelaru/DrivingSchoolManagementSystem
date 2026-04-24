package com.drivingschool.instructor.controller;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.dto.PageResponse;
import com.drivingschool.instructor.dto.InstructorRequest;
import com.drivingschool.instructor.dto.InstructorResponse;
import com.drivingschool.instructor.entity.Instructor;
import com.drivingschool.instructor.security.InstructorAuthorizationService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
@Tag(name = "Instructor Management", description = "APIs for managing instructors, including registration and retrieval")
public class InstructorController {
    private final InstructorService instructorService;
    private final InstructorAuthorizationService instructorAuthorizationService;

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
              description = "Retrieves detailed information about a specific instructor by identifier. "
                      + "Used by internal services and privileged roles.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Instructor found",
                    content = @Content(schema = @Schema(implementation = InstructorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Instructor not found")
    })
    @PreAuthorize("@instructorAuthz.isAdminOrService(authentication)")
    public ResponseEntity<ApiResult<InstructorResponse>> getInstructorById(
            @Parameter(description = "Unique instructor identifier", example = "1", required = true)
            @PathVariable Long id) {
        InstructorResponse response = instructorService.getInstructorById(id);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get instructor by ID", 
              description = "Retrieves detailed information about a specific instructor.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Instructor found",
                    content = @Content(schema = @Schema(implementation = InstructorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Instructor not found")
    })
    @PreAuthorize("@instructorAuthz.isInstructor(authentication)")
    public ResponseEntity<ApiResult<InstructorResponse>> getInstructor(
            Authentication authentication) {
        Long instructorId = instructorAuthorizationService.profileId(authentication);
        InstructorResponse response = instructorService.getInstructorById(instructorId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PutMapping("/me")
    @Operation(summary = "Update an instructor",
              description = "Updates instructor fields. License number and email must remain unique. Rating is not changed via this endpoint.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Instructor updated",
                    content = @Content(schema = @Schema(implementation = InstructorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "404", description = "Instructor not found"),
        @ApiResponse(responseCode = "409", description = "Duplicate license number or email")
    })
    @PreAuthorize("@instructorAuthz.isInstructor(authentication)")
    public ResponseEntity<ApiResult<InstructorResponse>> updateInstructor(
            @Valid @RequestBody InstructorRequest request,
            Authentication authentication) {
        Long instructorId = instructorAuthorizationService.profileId(authentication);
        InstructorResponse response = instructorService.updateInstructor(instructorId, request);
        return ResponseEntity.ok(ApiResult.success("Instructor updated successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update instructor information by ID (admin only)",
              description = "Updates existing instructor details by identifier. Intended for administrative use.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Instructor updated successfully",
                    content = @Content(schema = @Schema(implementation = InstructorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Instructor not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResult<InstructorResponse>> updateInstructorById(
            @Parameter(description = "Unique instructor identifier", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody InstructorRequest request) {
        InstructorResponse response = instructorService.updateInstructor(id, request);
        return ResponseEntity.ok(ApiResult.success("Instructor updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an instructor",
              description = "Deletes instructor if they have no courses in scheduling-service.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Instructor deleted"),
        @ApiResponse(responseCode = "404", description = "Instructor not found"),
        @ApiResponse(responseCode = "409", description = "Instructor still has assigned courses"),
        @ApiResponse(responseCode = "503", description = "Scheduling service unavailable; dependency check failed")
    })
    public ResponseEntity<ApiResult<Void>> deleteInstructor(
            @Parameter(description = "Instructor ID", required = true) @PathVariable Long id) {
        instructorService.deleteInstructor(id);
        return ResponseEntity.ok(ApiResult.success("Instructor deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get all instructors", 
              description = "Retrieves a paginated list of instructors in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Instructors retrieved successfully")
    })
    public ResponseEntity<ApiResult<PageResponse<InstructorResponse>>> getAllInstructors(
            @Parameter(description = "Page index (0-based)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size (overrides app.pagination.default-page-size)", example = "20")
            @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field: firstName, lastName, email, createdAt", example = "createdAt")
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc", example = "desc")
            @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        PageResponse<InstructorResponse> instructors = instructorService.getInstructorsPage(page, size, sortBy, sortDir);
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

