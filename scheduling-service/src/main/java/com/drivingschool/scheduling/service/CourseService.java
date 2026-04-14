package com.drivingschool.scheduling.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.common.dto.PageResponse;
import com.drivingschool.common.mapper.PageResponseMapper;
import com.drivingschool.common.pagination.PageableFactory;
import com.drivingschool.scheduling.pagination.CourseSortField;
import com.drivingschool.scheduling.dto.CourseRequest;
import com.drivingschool.scheduling.dto.CourseResponse;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.entity.Course;
import com.drivingschool.scheduling.entity.CourseTag;
import com.drivingschool.scheduling.mapper.CourseMapper;
import com.drivingschool.scheduling.repository.CourseRepository;
import com.drivingschool.scheduling.repository.CourseTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseTagRepository courseTagRepository;
    private final CourseMapper courseMapper;
    private final InstructorHelperService instructorHelperService;
    private final VehicleHelperService vehicleHelperService;
    private final com.drivingschool.scheduling.mapper.SchedulingMapper schedulingMapper;
    @Value("${app.pagination.default-page-size:20}")
    private int defaultPageSize;

    public CourseResponse createCourse(CourseRequest request) {
        log.info("Creating course: {}", request.name());
        
        validateCourseResources(request.instructorId(), request.vehicleId());
        Course course = courseMapper.toEntity(request);
        applyCourseTags(course, request.courseTagCodes());
        course = courseRepository.save(course);
        log.info("Course created with ID: {}", course.getId());
        return courseMapper.toResponse(course);
    }

    private void applyCourseTags(Course course, List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            course.getCourseTags().clear();
            return;
        }
        Set<String> uniqueCodes = new HashSet<>(codes);
        if (uniqueCodes.size() != codes.size()) {
            throw new BusinessException("Duplicate course tag codes in request", ErrorCode.INVALID_COURSE_TAG);
        }
        List<CourseTag> found = courseTagRepository.findByCodeIn(uniqueCodes);
        if (found.size() != uniqueCodes.size()) {
            throw new BusinessException("One or more course tag codes are invalid", ErrorCode.INVALID_COURSE_TAG);
        }
        course.getCourseTags().clear();
        course.getCourseTags().addAll(found);
    }

    private void validateCourseResources(Long instructorId, Long vehicleId) {
        instructorHelperService.getInstructorOrThrow(instructorId);
        log.debug("Instructor ID {} validated", instructorId);
        vehicleHelperService.validateVehicleForUse(vehicleId);
        log.debug("Vehicle ID {} validated for use", vehicleId);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        log.info("Fetching course with ID: {}", id);
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        return courseMapper.toResponse(course);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getCoursesPage(
            Long instructorId,
            Long vehicleId,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    ) {
        validateFiltersIfProvided(instructorId, vehicleId);
        Pageable pageable = PageableFactory.build(
                page, size, sortBy, sortDir, defaultPageSize, CourseSortField.class
        );
        Page<Course> coursePage = findCoursePageByFilters(instructorId, vehicleId, pageable);
        return PageResponseMapper.from(coursePage.map(courseMapper::toResponse));
    }

    private Page<Course> findCoursePageByFilters(Long instructorId, Long vehicleId, Pageable pageable) {
        if (instructorId != null && vehicleId != null) {
            return courseRepository.findByInstructorIdAndVehicleId(instructorId, vehicleId, pageable);
        } else if (instructorId != null) {
            return courseRepository.findByInstructorId(instructorId, pageable);
        } else if (vehicleId != null) {
            return courseRepository.findByVehicleId(vehicleId, pageable);
        } else {
            return courseRepository.findAll(pageable);
        }
    }

    private void validateFiltersIfProvided(Long instructorId, Long vehicleId) {
        if (instructorId != null) {
            instructorHelperService.getInstructorOrThrow(instructorId);
            log.debug("Instructor ID {} validated for filter", instructorId);
        }
        
        if (vehicleId != null) {
            vehicleHelperService.getVehicleOrThrow(vehicleId);
            log.debug("Vehicle ID {} validated for filter", vehicleId);
        }
    }

    public CourseResponse updateCourse(Long id, CourseRequest request) {
        log.info("Updating course with ID: {}", id);
        Course course = findCourseById(id);
        validateCourseResourcesForUpdate(course, request);
        courseMapper.updateEntity(course, request);
        applyCourseTags(course, request.courseTagCodes());
        course = courseRepository.save(course);
        log.info("Course updated with ID: {}", course.getId());
        return courseMapper.toResponse(course);
    }

    private Course findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    private void validateCourseResourcesForUpdate(Course existingCourse, CourseRequest request) {
        if (!existingCourse.getInstructorId().equals(request.instructorId())) {
            instructorHelperService.getInstructorOrThrow(request.instructorId());
            log.debug("Instructor ID {} validated for update", request.instructorId());
        }
        
        if (!existingCourse.getVehicleId().equals(request.vehicleId())) {
            vehicleHelperService.validateVehicleForUse(request.vehicleId());
            log.debug("Vehicle ID {} validated for update", request.vehicleId());
        }
    }

    public void deleteCourse(Long id) {
        log.info("Deleting course with ID: {}", id);
        Course course = findCourseById(id);
        validateCourseCanBeDeleted(course);
        courseRepository.deleteById(id);
        log.info("Course deleted with ID: {}", id);
    }

    private void validateCourseCanBeDeleted(Course course) {
        if (course.getLessons() != null && !course.getLessons().isEmpty()) {
            throw new BusinessException(
                    "Cannot delete course with existing lessons. Please remove or reassign lessons first.",
                    ErrorCode.COURSE_HAS_LESSONS);
        }
    }

    @Transactional(readOnly = true)
    public boolean instructorHasAnyCourse(Long instructorId) {
        return courseRepository.existsByInstructorId(instructorId);
    }

    @Transactional(readOnly = true)
    public boolean vehicleHasAnyCourse(Long vehicleId) {
        return courseRepository.existsByVehicleId(vehicleId);
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getCourseLessons(Long courseId) {
        log.info("Fetching lessons for course ID: {}", courseId);
        Course course = findCourseById(courseId);

        String instructorName = instructorHelperService.getInstructorName(course.getInstructorId());
        return course.getLessons().stream()
                .map(lesson -> schedulingMapper.toResponse(lesson, instructorName))
                .collect(Collectors.toList());
    }
}
