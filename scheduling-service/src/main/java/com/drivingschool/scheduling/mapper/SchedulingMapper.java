package com.drivingschool.scheduling.mapper;

import com.drivingschool.scheduling.dto.LessonRequest;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.entity.Lesson;
import org.springframework.stereotype.Component;

@Component
public class SchedulingMapper {
    public Lesson toEntity(LessonRequest request) {
        return Lesson.builder()
                .studentId(request.getStudentId())
                .instructorId(request.getInstructorId())
                .vehicleId(request.getVehicleId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .type(request.getType())
                .status(Lesson.LessonStatus.SCHEDULED)
                .build();
    }

    public LessonResponse toResponse(Lesson lesson, String instructorName) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .studentId(lesson.getStudentId())
                .instructorId(lesson.getInstructorId())
                .instructorName(instructorName)
                .vehicleId(lesson.getVehicleId())
                .startTime(lesson.getStartTime())
                .endTime(lesson.getEndTime())
                .type(lesson.getType())
                .status(lesson.getStatus())
                .createdAt(lesson.getCreatedAt())
                .build();
    }

    public void updateEntity(Lesson lesson, LessonRequest request) {
        lesson.setStudentId(request.getStudentId());
        lesson.setInstructorId(request.getInstructorId());
        lesson.setVehicleId(request.getVehicleId());
        lesson.setStartTime(request.getStartTime());
        lesson.setEndTime(request.getEndTime());
        lesson.setType(request.getType());
    }
}

