package com.drivingschool.scheduling.mapper;

import com.drivingschool.scheduling.dto.InstructorRequest;
import com.drivingschool.scheduling.dto.InstructorResponse;
import com.drivingschool.scheduling.dto.LessonRequest;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.entity.Instructor;
import com.drivingschool.scheduling.entity.Lesson;
import org.springframework.stereotype.Component;

@Component
public class SchedulingMapper {
    public Lesson toEntity(LessonRequest request, Instructor instructor) {
        return Lesson.builder()
                .studentId(request.getStudentId())
                .instructor(instructor)
                .vehicleId(request.getVehicleId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .type(request.getType())
                .status(Lesson.LessonStatus.SCHEDULED)
                .build();
    }

    public LessonResponse toResponse(Lesson lesson) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .studentId(lesson.getStudentId())
                .instructorId(lesson.getInstructor().getId())
                .instructorName(lesson.getInstructor().getFirstName() + " " + lesson.getInstructor().getLastName())
                .vehicleId(lesson.getVehicleId())
                .startTime(lesson.getStartTime())
                .endTime(lesson.getEndTime())
                .type(lesson.getType())
                .status(lesson.getStatus())
                .createdAt(lesson.getCreatedAt())
                .build();
    }

    public void updateEntity(Lesson lesson, LessonRequest request, Instructor instructor) {
        lesson.setStudentId(request.getStudentId());
        lesson.setInstructor(instructor);
        lesson.setVehicleId(request.getVehicleId());
        lesson.setStartTime(request.getStartTime());
        lesson.setEndTime(request.getEndTime());
        lesson.setType(request.getType());
    }

    public Instructor toEntity(InstructorRequest request) {
        return Instructor.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .licenseNumber(request.getLicenseNumber())
                .email(request.getEmail())
                .phone(request.getPhone())
                .specialization(request.getSpecialization())
                .rating(0.0)
                .build();
    }

    public InstructorResponse toResponse(Instructor instructor) {
        return InstructorResponse.builder()
                .id(instructor.getId())
                .firstName(instructor.getFirstName())
                .lastName(instructor.getLastName())
                .licenseNumber(instructor.getLicenseNumber())
                .email(instructor.getEmail())
                .phone(instructor.getPhone())
                .specialization(instructor.getSpecialization())
                .rating(instructor.getRating())
                .createdAt(instructor.getCreatedAt())
                .lastModifiedDate(instructor.getLastModifiedDate())
                .build();
    }
}

