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

    @NotNull(message = "Instructor ID is required")
    @Schema(description = "ID of the instructor teaching the lesson", example = "1")
    private Long instructorId;

    @Schema(description = "ID of the vehicle used for the lesson (optional)", example = "1")
    private Long vehicleId;

    @NotNull(message = "Start time is required")
    @Schema(description = "Lesson start date and time", example = "2027-01-01T10:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Schema(description = "Lesson end date and time", example = "2027-01-01T11:00:00")
    private LocalDateTime endTime;

    @NotNull(message = "Lesson type is required")
    @Schema(description = "Type of lesson", example = "PRACTICAL")
    private Lesson.LessonType type;
}

