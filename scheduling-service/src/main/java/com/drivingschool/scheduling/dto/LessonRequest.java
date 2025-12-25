package com.drivingschool.scheduling.dto;

import com.drivingschool.scheduling.entity.Lesson;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

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

    // Getters and Setters
    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Lesson.LessonType getType() {
        return type;
    }

    public void setType(Lesson.LessonType type) {
        this.type = type;
    }
}
