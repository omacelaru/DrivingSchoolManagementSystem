package com.drivingschool.scheduling.mapper;

import com.drivingschool.scheduling.dto.CourseRequest;
import com.drivingschool.scheduling.dto.CourseResponse;
import com.drivingschool.scheduling.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
    public Course toEntity(CourseRequest request) {
        return Course.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .instructorId(request.getInstructorId())
                .vehicleId(request.getVehicleId())
                .numberOfLessons(request.getNumberOfLessons())
                .courseType(request.getCourseType())
                .build();
    }

    public CourseResponse toResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .price(course.getPrice())
                .duration(course.getDuration()) // Calculated from lessons
                .numberOfLessons(course.getNumberOfLessons()) // Configured number of lessons
                .bookedLessons(course.getLessons() != null ? course.getLessons().size() : 0) // Actually booked
                .courseType(course.getCourseType())
                .instructorId(course.getInstructorId())
                .vehicleId(course.getVehicleId())
                .createdAt(course.getCreatedAt())
                .lastModifiedDate(course.getLastModifiedDate())
                .build();
    }
}

