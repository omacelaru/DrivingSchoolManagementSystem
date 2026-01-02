package com.drivingschool.scheduling.service;

import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.scheduling.dto.CourseRequest;
import com.drivingschool.scheduling.dto.CourseResponse;
import com.drivingschool.scheduling.entity.Course;
import com.drivingschool.scheduling.mapper.CourseMapper;
import com.drivingschool.scheduling.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final InstructorHelperService instructorHelperService;
    private final com.drivingschool.scheduling.mapper.SchedulingMapper schedulingMapper;

    public CourseResponse createCourse(CourseRequest request) {
        log.info("Creating course: {}", request.getName());
        Course course = courseMapper.toEntity(request);
        course = courseRepository.save(course);
        log.info("Course created with ID: {}", course.getId());
        return courseMapper.toResponse(course);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        log.info("Fetching course with ID: {}", id);
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        return courseMapper.toResponse(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses(Long instructorId, Long vehicleId) {
        log.info("Fetching courses with filters - instructorId: {}, vehicleId: {}", instructorId, vehicleId);
        List<Course> courses;
        
        if (instructorId != null && vehicleId != null) {
            courses = courseRepository.findByInstructorIdAndVehicleId(instructorId, vehicleId);
        } else if (instructorId != null) {
            courses = courseRepository.findByInstructorId(instructorId);
        } else if (vehicleId != null) {
            courses = courseRepository.findByVehicleId(vehicleId);
        } else {
            courses = courseRepository.findAll();
        }
        
        return courses.stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CourseResponse updateCourse(Long id, CourseRequest request) {
        log.info("Updating course with ID: {}", id);
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setPrice(request.getPrice());
        course.setInstructorId(request.getInstructorId());
        course.setVehicleId(request.getVehicleId());
        
        course = courseRepository.save(course);
        log.info("Course updated with ID: {}", course.getId());
        return courseMapper.toResponse(course);
    }

    public void deleteCourse(Long id) {
        log.info("Deleting course with ID: {}", id);
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        
        // Check if course has lessons
        if (course.getLessons() != null && !course.getLessons().isEmpty()) {
            throw new com.drivingschool.common.exception.BusinessException(
                    "Cannot delete course with existing lessons. Please remove or reassign lessons first.",
                    "COURSE_HAS_LESSONS");
        }
        
        courseRepository.deleteById(id);
        log.info("Course deleted with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<com.drivingschool.scheduling.dto.LessonResponse> getCourseLessons(Long courseId) {
        log.info("Fetching lessons for course ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        
        // Force load lessons
        course.getLessons().size();
        
        return course.getLessons().stream()
                .map(lesson -> {
                    String instructorName = instructorHelperService.getInstructorName(lesson.getInstructorId());
                    return schedulingMapper.toResponse(lesson, instructorName);
                })
                .collect(Collectors.toList());
    }
}
