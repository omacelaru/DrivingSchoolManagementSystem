package com.drivingschool.scheduling.mapper;

import com.drivingschool.common.mapstruct.IgnoreJpaIdAndTimestamps;
import com.drivingschool.scheduling.dto.LessonRequest;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.entity.Course;
import com.drivingschool.scheduling.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface SchedulingMapper {

    @IgnoreJpaIdAndTimestamps
    @Mapping(target = "studentId", ignore = true)
    @Mapping(target = "status", constant = "SCHEDULED")
    Lesson toEntity(LessonRequest request, Course course, LocalDateTime endTime);

    default LessonResponse toResponse(Lesson lesson, String instructorName) {
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

    @IgnoreJpaIdAndTimestamps
    @Mapping(target = "studentId", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(@MappingTarget Lesson lesson, LessonRequest request, Course course, LocalDateTime endTime);
}
