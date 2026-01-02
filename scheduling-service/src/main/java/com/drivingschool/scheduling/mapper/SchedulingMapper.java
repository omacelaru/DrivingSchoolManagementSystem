package com.drivingschool.scheduling.mapper;

import com.drivingschool.scheduling.dto.LessonRequest;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.entity.Course;
import com.drivingschool.scheduling.entity.Lesson;
import org.springframework.stereotype.Component;

@Component
public class SchedulingMapper {
    public Lesson toEntity(LessonRequest request, Course course) {
        return Lesson.builder()
                .studentId(request.getStudentId())
                .course(course)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(Lesson.LessonStatus.SCHEDULED)
                .build();
    }

    public LessonResponse toResponse(Lesson lesson, String instructorName) {
        Course course = lesson.getCourse();
        return LessonResponse.builder()
                .id(lesson.getId())
                .studentId(lesson.getStudentId())
                .instructorId(course != null ? course.getInstructorId() : null)
                .instructorName(instructorName)
                .vehicleId(course != null ? course.getVehicleId() : null)
                .courseId(course != null ? course.getId() : null)
                .startTime(lesson.getStartTime())
                .endTime(lesson.getEndTime())
                .status(lesson.getStatus())
                .createdAt(lesson.getCreatedAt())
                .build();
    }

    public void updateEntity(Lesson lesson, LessonRequest request, Course course) {
        lesson.setStudentId(request.getStudentId());
        lesson.setCourse(course);
        lesson.setStartTime(request.getStartTime());
        lesson.setEndTime(request.getEndTime());
    }
}

