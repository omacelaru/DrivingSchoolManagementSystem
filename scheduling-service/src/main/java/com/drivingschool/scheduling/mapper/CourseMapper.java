package com.drivingschool.scheduling.mapper;

import com.drivingschool.scheduling.dto.CourseRequest;
import com.drivingschool.scheduling.dto.CourseResponse;
import com.drivingschool.scheduling.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
    public Course toEntity(CourseRequest request) {
        return Course.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .instructorId(request.instructorId())
                .vehicleId(request.vehicleId())
                .numberOfLessons(request.numberOfLessons())
                .courseType(request.courseType())
                .build();
    }

    public CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getPrice(),
                course.getNumberOfLessons(), // Configured number of lessons
                course.getCourseType(),
                course.getInstructorId(),
                course.getVehicleId(),
                course.getCreatedAt(),
                course.getLastModifiedDate()
        );
    }

    public void updateEntity(Course course, CourseRequest request) {
        course.setName(request.name());
        course.setDescription(request.description());
        course.setPrice(request.price());
        course.setInstructorId(request.instructorId());
        course.setVehicleId(request.vehicleId());
        course.setNumberOfLessons(request.numberOfLessons());
        course.setCourseType(request.courseType());
    }
}

