package com.drivingschool.scheduling.dto;

import com.drivingschool.scheduling.entity.Lesson;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Instructor ID is required")
    private Long instructorId;

    private Long vehicleId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotNull(message = "Lesson type is required")
    private Lesson.LessonType type;
}

