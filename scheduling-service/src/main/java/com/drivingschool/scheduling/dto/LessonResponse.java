package com.drivingschool.scheduling.dto;

import com.drivingschool.scheduling.entity.Lesson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {
    private Long id;
    private Long studentId;
    private Long instructorId;
    private String instructorName;
    private Long vehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Lesson.LessonType type;
    private Lesson.LessonStatus status;
    private LocalDateTime createdAt;
}

