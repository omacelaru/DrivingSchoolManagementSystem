package com.drivingschool.scheduling.dto;

import com.drivingschool.scheduling.entity.Lesson;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response DTO containing lesson information")
public record LessonResponse(
    @Schema(description = "Unique lesson identifier", example = "1")
    Long id,
    
    @Schema(description = "ID of the student taking the lesson", example = "1")
    Long studentId,
    
    @Schema(description = "ID of the instructor teaching the lesson", example = "1")
    Long instructorId,
    
    @Schema(description = "Full name of the instructor", example = "John Smith")
    String instructorName,
    
    @Schema(description = "ID of the vehicle used for the lesson", example = "1")
    Long vehicleId,
    
    @Schema(description = "ID of the course this lesson belongs to (if part of a course)", example = "1")
    Long courseId,
    
    @Schema(description = "Lesson start date and time", example = "2027-01-01T10:00:00")
    LocalDateTime startTime,
    
    @Schema(description = "Lesson end date and time", example = "2027-01-01T11:00:00")
    LocalDateTime endTime,
    
    @Schema(description = "Current status of the lesson", example = "SCHEDULED")
    Lesson.LessonStatus status,
    
    @Schema(description = "Date and time when lesson was created", example = "2027-01-01T10:30:00")
    LocalDateTime createdAt
) {
}

