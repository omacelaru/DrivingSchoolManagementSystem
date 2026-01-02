package com.drivingschool.scheduling.dto;

import com.drivingschool.scheduling.entity.Lesson;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Request DTO for creating or updating a lesson")
public class LessonRequest {
    @NotNull(message = "Student ID is required")
    @Schema(description = "ID of the student taking the lesson", example = "1")
    private Long studentId;

    @Schema(description = "ID of the course this lesson belongs to. If provided, instructorId, vehicleId, and type will be taken from the course.", example = "1")
    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Start time is required")
    @Schema(description = "Lesson start date and time", example = "2027-01-01T10:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Schema(description = "Lesson end date and time", example = "2027-01-01T11:00:00")
    private LocalDateTime endTime;
}

