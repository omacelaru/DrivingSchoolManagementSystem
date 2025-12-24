package com.drivingschool.scheduling.controller;

import com.drivingschool.common.dto.ApiResponse;
import com.drivingschool.scheduling.dto.LessonRequest;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.entity.Instructor;
import com.drivingschool.scheduling.service.SchedulingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Tag(name = "Scheduling Management", description = "APIs for managing lessons and scheduling")
public class SchedulingController {
    private final SchedulingService schedulingService;

    @PostMapping
    @Operation(summary = "Book a new lesson", description = "Creates a new lesson booking")
    public ResponseEntity<ApiResponse<LessonResponse>> bookLesson(
            @Valid @RequestBody LessonRequest request) {
        LessonResponse response = schedulingService.bookLesson(request);
        return ResponseEntity.ok(ApiResponse.success("Lesson booked successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lesson by ID", description = "Retrieves lesson details")
    public ResponseEntity<ApiResponse<LessonResponse>> getLesson(
            @Parameter(description = "Lesson ID") @PathVariable Long id) {
        LessonResponse response = schedulingService.getLessonById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update lesson", description = "Updates lesson details (reschedule)")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(
            @Parameter(description = "Lesson ID") @PathVariable Long id,
            @Valid @RequestBody LessonRequest request) {
        LessonResponse response = schedulingService.updateLesson(id, request);
        return ResponseEntity.ok(ApiResponse.success("Lesson updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel lesson", description = "Cancels a scheduled lesson")
    public ResponseEntity<ApiResponse<Void>> cancelLesson(
            @Parameter(description = "Lesson ID") @PathVariable Long id) {
        schedulingService.cancelLesson(id);
        return ResponseEntity.ok(ApiResponse.success("Lesson cancelled successfully", null));
    }

    @GetMapping("/instructors/{instructorId}")
    @Operation(summary = "Get instructor lessons", description = "Retrieves all lessons for an instructor")
    public ResponseEntity<ApiResponse<List<LessonResponse>>> getInstructorLessons(
            @Parameter(description = "Instructor ID") @PathVariable Long instructorId) {
        List<LessonResponse> lessons = schedulingService.getInstructorLessons(instructorId);
        return ResponseEntity.ok(ApiResponse.success(lessons));
    }

    @GetMapping("/instructors/available")
    @Operation(summary = "Get available instructors", description = "Finds instructors available for a time slot")
    public ResponseEntity<ApiResponse<List<Instructor>>> getAvailableInstructors(
            @Parameter(description = "Start time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<Instructor> instructors = schedulingService.getAvailableInstructors(startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(instructors));
    }
}

