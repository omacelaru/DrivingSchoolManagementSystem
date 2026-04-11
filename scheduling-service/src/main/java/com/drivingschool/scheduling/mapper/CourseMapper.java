package com.drivingschool.scheduling.mapper;

import com.drivingschool.scheduling.dto.CourseRequest;
import com.drivingschool.scheduling.dto.CourseResponse;
import com.drivingschool.scheduling.entity.Course;
import com.drivingschool.scheduling.entity.CourseTag;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
                course.getLastModifiedDate(),
                toCourseTagCodes(course.getCourseTags())
        );
    }

    private List<String> toCourseTagCodes(Set<CourseTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(CourseTag::getCode)
                .sorted(Comparator.naturalOrder())
                .toList();
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

