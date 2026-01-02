package com.drivingschool.scheduling.dto;

import com.drivingschool.scheduling.entity.Lesson;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing lesson information")
public class LessonResponse {
    @Schema(description = "Unique lesson identifier", example = "1")
    private Long id;
    
    @Schema(description = "ID of the student taking the lesson", example = "1")
    private Long studentId;
    
    @Schema(description = "ID of the instructor teaching the lesson", example = "1")
    private Long instructorId;
    
    @Schema(description = "Full name of the instructor", example = "John Smith")
    private String instructorName;
    
    @Schema(description = "ID of the vehicle used for the lesson", example = "1")
    private Long vehicleId;
    
    @Schema(description = "Lesson start date and time", example = "2025-01-01T10:00:00")
    private LocalDateTime startTime;
    
    @Schema(description = "Lesson end date and time", example = "2025-01-01T11:00:00")
    private LocalDateTime endTime;
    
    @Schema(description = "Type of lesson", example = "PRACTICAL")
    private Lesson.LessonType type;
    
    @Schema(description = "Current status of the lesson", example = "SCHEDULED")
    private Lesson.LessonStatus status;
    
    @Schema(description = "Date and time when lesson was created", example = "2025-01-01T10:30:00")
    private LocalDateTime createdAt;
}

