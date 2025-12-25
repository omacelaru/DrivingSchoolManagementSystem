package com.drivingschool.scheduling.dto;

import com.drivingschool.scheduling.entity.Lesson;

import java.time.LocalDateTime;

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

    public LessonResponse() {
    }

    public LessonResponse(Long id, Long studentId, Long instructorId, String instructorName, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime, Lesson.LessonType type, Lesson.LessonStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.instructorId = instructorId;
        this.instructorName = instructorName;
        this.vehicleId = vehicleId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
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

    public Lesson.LessonStatus getStatus() {
        return status;
    }

    public void setStatus(Lesson.LessonStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
