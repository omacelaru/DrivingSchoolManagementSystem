package com.drivingschool.scheduling.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Schema(description = "Request DTO for creating or updating a lesson")
public record LessonRequest(
    @NotNull(message = "Student ID is required")
    @Positive(message = "Student ID must be positive")
    @Schema(description = "ID of the student taking the lesson", example = "1")
    Long studentId,

    @NotNull(message = "Course ID is required")
    @Positive(message = "Course ID must be positive")
    @Schema(description = "ID of the course this lesson belongs to. If provided, instructorId, vehicleId, and type will be taken from the course.", example = "1")
    Long courseId,

    @NotNull(message = "Start time is required")
    @Schema(description = "Lesson start date and time", example = "2027-01-01T10:00:00")
    LocalDateTime startTime,

    @Schema(description = "Lesson end date and time. If not provided, will be calculated as startTime + 1 hour 30 minutes", example = "2027-01-01T11:30:00")
    LocalDateTime endTime
) {
}

