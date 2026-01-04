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
                .studentId(request.studentId())
                .course(course)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .status(Lesson.LessonStatus.SCHEDULED)
                .build();
    }

    public LessonResponse toResponse(Lesson lesson, String instructorName) {
        Course course = lesson.getCourse();
        return new LessonResponse(
                lesson.getId(),
                lesson.getStudentId(),
                course != null ? course.getInstructorId() : null,
                instructorName,
                course != null ? course.getVehicleId() : null,
                course != null ? course.getId() : null,
                lesson.getStartTime(),
                lesson.getEndTime(),
                lesson.getStatus(),
                lesson.getCreatedAt()
        );
    }

    public void updateEntity(Lesson lesson, LessonRequest request, Course course) {
        lesson.setStudentId(request.studentId());
        lesson.setCourse(course);
        lesson.setStartTime(request.startTime());
        lesson.setEndTime(request.endTime());
    }
}

