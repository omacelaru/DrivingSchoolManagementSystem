package com.drivingschool.scheduling.mapper;

import com.drivingschool.scheduling.dto.LessonRequest;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.entity.Instructor;
import com.drivingschool.scheduling.entity.Lesson;
import org.springframework.stereotype.Component;

@Component
public class SchedulingMapper {
    public Lesson toEntity(LessonRequest request, Instructor instructor) {
        Lesson lesson = new Lesson();
        lesson.setStudentId(request.getStudentId());
        lesson.setInstructor(instructor);
        lesson.setVehicleId(request.getVehicleId());
        lesson.setStartTime(request.getStartTime());
        lesson.setEndTime(request.getEndTime());
        lesson.setType(request.getType());
        lesson.setStatus(Lesson.LessonStatus.SCHEDULED);
        return lesson;
    }

    public LessonResponse toResponse(Lesson lesson) {
        LessonResponse response = new LessonResponse();
        response.setId(lesson.getId());
        response.setStudentId(lesson.getStudentId());
        if (lesson.getInstructor() != null) {
            response.setInstructorId(lesson.getInstructor().getId());
            response.setInstructorName(lesson.getInstructor().getFirstName() + " " + lesson.getInstructor().getLastName());
        }
        response.setVehicleId(lesson.getVehicleId());
        response.setStartTime(lesson.getStartTime());
        response.setEndTime(lesson.getEndTime());
        response.setType(lesson.getType());
        response.setStatus(lesson.getStatus());
        response.setCreatedAt(lesson.getCreatedAt());
        return response;
    }

    public void updateEntity(Lesson lesson, LessonRequest request, Instructor instructor) {
        lesson.setStudentId(request.getStudentId());
        lesson.setInstructor(instructor);
        lesson.setVehicleId(request.getVehicleId());
        lesson.setStartTime(request.getStartTime());
        lesson.setEndTime(request.getEndTime());
        lesson.setType(request.getType());
    }
}
