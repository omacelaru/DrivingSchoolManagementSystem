package com.drivingschool.scheduling.controller;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.scheduling.dto.CourseRequest;
import com.drivingschool.scheduling.dto.CourseResponse;
import com.drivingschool.scheduling.service.CourseService;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Course Management", description = "APIs for managing courses")
public class CourseController {
    private final CourseService courseService;

    @PostMapping
    @Operation(summary = "Create a new course",
              description = "Creates a new course with specified instructor, vehicle, number of lessons, and price.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Course created successfully",
                    content = @Content(schema = @Schema(implementation = CourseResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation failed")
    })
    public ResponseEntity<ApiResult<CourseResponse>> createCourse(
            @Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("Course created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID",
              description = "Retrieves detailed information about a specific course.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Course found",
                    content = @Content(schema = @Schema(implementation = CourseResponse.class))),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<ApiResult<CourseResponse>> getCourse(
            @Parameter(description = "Unique course identifier", example = "1", required = true)
            @PathVariable Long id) {
        CourseResponse response = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all courses",
              description = "Retrieves all available courses. Can be filtered by instructorId and/or vehicleId.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Courses retrieved successfully")
    })
    public ResponseEntity<ApiResult<List<CourseResponse>>> getAllCourses(
            @Parameter(description = "Filter by instructor ID", example = "1")
            @RequestParam(required = false) Long instructorId,
            @Parameter(description = "Filter by vehicle ID", example = "1")
            @RequestParam(required = false) Long vehicleId) {
        List<CourseResponse> courses = courseService.getAllCourses(instructorId, vehicleId);
        return ResponseEntity.ok(ApiResult.success(courses));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a course",
              description = "Updates the details of an existing course.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Course updated successfully",
                    content = @Content(schema = @Schema(implementation = CourseResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation failed"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<ApiResult<CourseResponse>> updateCourse(
            @Parameter(description = "Unique course identifier", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResult.success("Course updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a course",
              description = "Deletes a course by its ID. Cannot delete courses that have lessons.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Course deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "409", description = "Cannot delete course with existing lessons")
    })
    public ResponseEntity<ApiResult<Void>> deleteCourse(
            @Parameter(description = "Unique course identifier", example = "1", required = true)
            @PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResult.success("Course deleted successfully", null));
    }

    @GetMapping("/{id}/lessons")
    @Operation(summary = "Get lessons for a course",
              description = "Retrieves all lessons associated with a specific course.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<ApiResult<List<com.drivingschool.scheduling.dto.LessonResponse>>> getCourseLessons(
            @Parameter(description = "Unique course identifier", example = "1", required = true)
            @PathVariable Long id) {
        List<com.drivingschool.scheduling.dto.LessonResponse> lessons = courseService.getCourseLessons(id);
        return ResponseEntity.ok(ApiResult.success(lessons));
    }
}

