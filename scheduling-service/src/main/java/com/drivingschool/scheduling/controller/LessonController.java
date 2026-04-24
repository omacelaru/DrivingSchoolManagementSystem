package com.drivingschool.scheduling.controller;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.scheduling.dto.LessonRequest;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.entity.Lesson;
import com.drivingschool.scheduling.security.LessonAuthorizationService;
import com.drivingschool.scheduling.service.LessonService;
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
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Tag(name = "Scheduling Management", description = "APIs for managing lessons and scheduling, including booking, rescheduling, cancellation, and finding available instructors")
public class LessonController {
    private final LessonService lessonService;
    private final LessonAuthorizationService lessonAuthorizationService;

    @PostMapping
    @Operation(summary = "Book a new lesson", 
              description = "Creates a new lesson booking. Validates instructor and vehicle availability, and checks for conflicts.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lesson booked successfully",
                    content = @Content(schema = @Schema(implementation = LessonResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation failed"),
        @ApiResponse(responseCode = "409", description = "Instructor or vehicle not available for the requested time slot")
    })
    @PreAuthorize("@lessonAuthz.isStudent(authentication)")
    public ResponseEntity<ApiResult<LessonResponse>> bookLesson(
            @Valid @RequestBody LessonRequest request,
            Authentication authentication) {
        Long studentId = lessonAuthorizationService.profileId(authentication);
        LessonResponse response = lessonService.bookLesson(request, studentId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("Lesson booked successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lesson by ID", 
              description = "Retrieves detailed information about a specific lesson, including student, instructor, and vehicle details.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lesson found",
                    content = @Content(schema = @Schema(implementation = LessonResponse.class))),
        @ApiResponse(responseCode = "404", description = "Lesson not found")
    })
    @PreAuthorize("@lessonAuthz.canAccessLesson(#id, authentication)")
    public ResponseEntity<ApiResult<LessonResponse>> getLesson(
            @Parameter(description = "Unique lesson identifier", example = "1", required = true) 
            @PathVariable Long id) {
        LessonResponse response = lessonService.getLessonById(id);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update lesson (reschedule)", 
              description = "Updates lesson details, typically used for rescheduling. Validates new time slot availability.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lesson updated successfully",
                    content = @Content(schema = @Schema(implementation = LessonResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Lesson not found"),
        @ApiResponse(responseCode = "409", description = "New time slot not available")
    })
    @PreAuthorize("@lessonAuthz.canAccessLesson(#id, authentication) and @lessonAuthz.isStudent(authentication)")
    public ResponseEntity<ApiResult<LessonResponse>> updateLesson(
            @Parameter(description = "Unique lesson identifier", example = "1", required = true) 
            @PathVariable Long id,
            @Valid @RequestBody LessonRequest request,
            Authentication authentication) {
        Long studentId = lessonAuthorizationService.profileId(authentication);
        LessonResponse response = lessonService.updateLesson(id, request, studentId);
        return ResponseEntity.ok(ApiResult.success("Lesson updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel lesson", 
              description = "Cancels a scheduled lesson. The lesson status will be updated to CANCELLED.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lesson cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Lesson not found"),
        @ApiResponse(responseCode = "409", description = "Cannot cancel lesson that has already started or completed")
    })
    @PreAuthorize("@lessonAuthz.canAccessLesson(#id, authentication)")
    public ResponseEntity<ApiResult<Void>> cancelLesson(
            @Parameter(description = "Unique lesson identifier", example = "1", required = true) 
            @PathVariable Long id) {
        lessonService.cancelLesson(id);
        return ResponseEntity.ok(ApiResult.success("Lesson cancelled successfully", null));
    }

    @GetMapping("/instructors/me")
    @Operation(summary = "Get instructor lessons", 
              description = "Retrieves all lessons scheduled for a specific instructor, including past and future lessons.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Instructor not found")
    })
    @PreAuthorize("@lessonAuthz.isInstructor(authentication)")
    public ResponseEntity<ApiResult<List<LessonResponse>>> getInstructorLessons(
            Authentication authentication) {
        Long authorizedInstructorId = lessonAuthorizationService.profileId(authentication);
        List<LessonResponse> lessons = lessonService.getInstructorLessons(authorizedInstructorId);
        return ResponseEntity.ok(ApiResult.success(lessons));
    }

    @GetMapping("/instructors/me/availability")
    @Operation(summary = "Check instructor availability", 
              description = "Checks if an instructor is available for a specific time slot by verifying lesson conflicts.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability check completed",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "400", description = "Invalid date/time format")
    })
    @PreAuthorize("@lessonAuthz.isInstructor(authentication)")
    public ResponseEntity<ApiResult<Boolean>> isInstructorAvailable(
            @Parameter(description = "Start date and time (ISO format)", example = "2027-01-01T10:00:00", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End date and time (ISO format)", example = "2027-01-01T11:00:00", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            Authentication authentication) {
        Long authorizedInstructorId = lessonAuthorizationService.profileId(authentication);
        Boolean isAvailable = lessonService.isInstructorAvailable(authorizedInstructorId, startTime, endTime);
        return ResponseEntity.ok(ApiResult.success(isAvailable));
    }

    @GetMapping("/vehicles/{vehicleId}/availability")
    @Operation(summary = "Check vehicle availability", 
              description = "Checks if a vehicle is available for a specific time slot by verifying lesson conflicts.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability check completed",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "400", description = "Invalid date/time format or missing required parameters")
    })
    public ResponseEntity<ApiResult<Boolean>> checkVehicleAvailability(
            @Parameter(description = "Unique vehicle identifier", example = "1", required = true) 
            @PathVariable Long vehicleId,
            @Parameter(description = "Start date and time (ISO format)", example = "2027-01-01T10:00:00", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End date and time (ISO format)", example = "2027-01-01T11:30:00", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Boolean isAvailable = lessonService.isVehicleAvailable(vehicleId, startTime, endTime);
        return ResponseEntity.ok(ApiResult.success(isAvailable));
    }

    @GetMapping("/students/me")
    @Operation(summary = "Get student lessons",
              description = "Retrieves all lessons for a specific student. Can be optionally filtered by status.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @PreAuthorize("@lessonAuthz.isStudent(authentication)")
    public ResponseEntity<ApiResult<List<LessonResponse>>> getStudentLessons(
            @Parameter(description = "Filter by lesson status (SCHEDULED, COMPLETED, CANCELLED, NO_SHOW)", example = "SCHEDULED")
            @RequestParam(required = false) Lesson.LessonStatus status,
            Authentication authentication) {
        Long authorizedStudentId = lessonAuthorizationService.profileId(authentication);
        List<LessonResponse> lessons = lessonService.getStudentLessons(authorizedStudentId, status);
        return ResponseEntity.ok(ApiResult.success(lessons));
    }

    @GetMapping("/students/me/upcoming")
    @Operation(summary = "Get upcoming lessons for student",
              description = "Retrieves all upcoming (future) lessons for a specific student.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Upcoming lessons retrieved successfully")
    })
    @PreAuthorize("@lessonAuthz.isStudent(authentication)")
    public ResponseEntity<ApiResult<List<LessonResponse>>> getUpcomingLessonsByStudent(
            Authentication authentication) {
        Long authorizedStudentId = lessonAuthorizationService.profileId(authentication);
        List<LessonResponse> lessons = lessonService.getUpcomingLessonsByStudent(authorizedStudentId);
        return ResponseEntity.ok(ApiResult.success(lessons));
    }

    @GetMapping("/courses/{courseId}")
    @Operation(summary = "Get lessons for a course",
              description = "Retrieves all lessons associated with a specific course.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<ApiResult<List<LessonResponse>>> getLessonsByCourse(
            @Parameter(description = "Unique course identifier", example = "1", required = true)
            @PathVariable Long courseId) {
        List<LessonResponse> lessons = lessonService.getLessonsByCourse(courseId);
        return ResponseEntity.ok(ApiResult.success(lessons));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get lessons by date range",
              description = "Retrieves all lessons within a specific date range.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date/time format")
    })
    public ResponseEntity<ApiResult<List<LessonResponse>>> getLessonsByDateRange(
            @Parameter(description = "Start date and time (ISO format)", example = "2027-01-01T00:00:00", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End date and time (ISO format)", example = "2027-01-31T23:59:59", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<LessonResponse> lessons = lessonService.getLessonsByDateRange(startTime, endTime);
        return ResponseEntity.ok(ApiResult.success(lessons));
    }

}

