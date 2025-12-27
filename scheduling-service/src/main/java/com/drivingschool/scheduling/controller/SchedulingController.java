package com.drivingschool.scheduling.controller;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.scheduling.dto.LessonRequest;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.entity.Instructor;
import com.drivingschool.scheduling.service.SchedulingService;
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
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Tag(name = "Scheduling Management", description = "APIs for managing lessons and scheduling, including booking, rescheduling, cancellation, and finding available instructors")
public class SchedulingController {
    private final SchedulingService schedulingService;

    @PostMapping
    @Operation(summary = "Book a new lesson", 
              description = "Creates a new lesson booking. Validates instructor and vehicle availability, and checks for conflicts.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lesson booked successfully",
                    content = @Content(schema = @Schema(implementation = LessonResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation failed"),
        @ApiResponse(responseCode = "409", description = "Instructor or vehicle not available for the requested time slot")
    })
    public ResponseEntity<ApiResult<LessonResponse>> bookLesson(
            @Valid @RequestBody LessonRequest request) {
        LessonResponse response = schedulingService.bookLesson(request);
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
    public ResponseEntity<ApiResult<LessonResponse>> getLesson(
            @Parameter(description = "Unique lesson identifier", example = "1", required = true) 
            @PathVariable Long id) {
        LessonResponse response = schedulingService.getLessonById(id);
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
    public ResponseEntity<ApiResult<LessonResponse>> updateLesson(
            @Parameter(description = "Unique lesson identifier", example = "1", required = true) 
            @PathVariable Long id,
            @Valid @RequestBody LessonRequest request) {
        LessonResponse response = schedulingService.updateLesson(id, request);
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
    public ResponseEntity<ApiResult<Void>> cancelLesson(
            @Parameter(description = "Unique lesson identifier", example = "1", required = true) 
            @PathVariable Long id) {
        schedulingService.cancelLesson(id);
        return ResponseEntity.ok(ApiResult.success("Lesson cancelled successfully", null));
    }

    @GetMapping("/instructors/{instructorId}")
    @Operation(summary = "Get instructor lessons", 
              description = "Retrieves all lessons scheduled for a specific instructor, including past and future lessons.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Instructor not found")
    })
    public ResponseEntity<ApiResult<List<LessonResponse>>> getInstructorLessons(
            @Parameter(description = "Unique instructor identifier", example = "1", required = true) 
            @PathVariable Long instructorId) {
        List<LessonResponse> lessons = schedulingService.getInstructorLessons(instructorId);
        return ResponseEntity.ok(ApiResult.success(lessons));
    }

    @GetMapping("/instructors/available")
    @Operation(summary = "Get available instructors", 
              description = "Finds all instructors who are available for a specific time slot. Checks for existing lesson conflicts.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Available instructors retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date/time format")
    })
    public ResponseEntity<ApiResult<List<Instructor>>> getAvailableInstructors(
            @Parameter(description = "Start date and time (ISO format)", example = "2024-12-20T10:00:00", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End date and time (ISO format)", example = "2024-12-20T11:00:00", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<Instructor> instructors = schedulingService.getAvailableInstructors(startTime, endTime);
        return ResponseEntity.ok(ApiResult.success(instructors));
    }
}

