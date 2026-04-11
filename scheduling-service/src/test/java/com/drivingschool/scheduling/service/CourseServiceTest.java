package com.drivingschool.scheduling.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.scheduling.dto.CourseRequest;
import com.drivingschool.scheduling.dto.CourseResponse;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.entity.Course;
import com.drivingschool.scheduling.entity.Lesson;
import com.drivingschool.scheduling.fixture.CourseFixture;
import com.drivingschool.scheduling.fixture.InstructorResponseFixture;
import com.drivingschool.scheduling.fixture.LessonFixture;
import com.drivingschool.scheduling.fixture.VehicleResponseFixture;
import com.drivingschool.scheduling.mapper.CourseMapper;
import com.drivingschool.scheduling.mapper.SchedulingMapper;
import org.mapstruct.factory.Mappers;
import com.drivingschool.scheduling.entity.CourseTag;
import com.drivingschool.scheduling.repository.CourseRepository;
import com.drivingschool.scheduling.repository.CourseTagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseTagRepository courseTagRepository;

    private final CourseMapper courseMapper = Mappers.getMapper(CourseMapper.class);

    @Mock
    private InstructorHelperService instructorHelperService;

    @Mock
    private VehicleHelperService vehicleHelperService;

    private final SchedulingMapper schedulingMapper = Mappers.getMapper(SchedulingMapper.class);

    private CourseService courseService;

    private CourseRequest courseRequest;
    private Course course;

    @BeforeEach
    void setUp() {
        courseRequest = CourseFixture.courseRequest();
        course = CourseFixture.course();

        courseService = new CourseService(
                courseRepository,
                courseTagRepository,
                courseMapper,
                instructorHelperService,
                vehicleHelperService,
                schedulingMapper
        );
    }

    @Test
    void whenCreateCourse_thenReturnsCourseResponse() {
        // Given
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = CourseFixture.defaultVehicleId();
        Long courseId = CourseFixture.defaultCourseId();
        String courseName = CourseFixture.defaultName();

        when(instructorHelperService.getInstructorOrThrow(instructorId))
                .thenReturn(InstructorResponseFixture.instructorResponse());
        doNothing().when(vehicleHelperService).validateVehicleForUse(vehicleId);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course saved = invocation.getArgument(0);
            saved.setId(courseId);
            return saved;
        });

        // When
        CourseResponse result = courseService.createCourse(courseRequest);

        // Then
        assertNotNull(result);
        assertEquals(courseName, result.name());
        assertEquals(courseId, result.id());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void whenCreateCourseWithNonExistentInstructorId_thenThrowsResourceNotFoundException() {
        // Given
        Long instructorId = CourseFixture.defaultInstructorId();

        when(instructorHelperService.getInstructorOrThrow(instructorId))
                .thenThrow(new ResourceNotFoundException("Instructor", instructorId));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> courseService.createCourse(courseRequest));
    }

    @Test
    void whenCreateCourseWithUnavailableVehicle_thenThrowsBusinessException() {
        // Given
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = CourseFixture.defaultVehicleId();

        when(instructorHelperService.getInstructorOrThrow(instructorId))
                .thenReturn(InstructorResponseFixture.instructorResponse());
        doThrow(new BusinessException("Vehicle not available", ErrorCode.VEHICLE_NOT_AVAILABLE))
                .when(vehicleHelperService).validateVehicleForUse(vehicleId);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> courseService.createCourse(courseRequest));

        assertEquals(ErrorCode.VEHICLE_NOT_AVAILABLE.getCode(), exception.getErrorCode());
    }

    @Test
    void whenCreateCourseWithInvalidTag_thenThrowsBusinessException() {
        CourseRequest request = new CourseRequest(
                CourseFixture.defaultName(),
                CourseFixture.defaultDescription(),
                CourseFixture.defaultPrice(),
                CourseFixture.defaultInstructorId(),
                CourseFixture.defaultVehicleId(),
                CourseFixture.defaultNumberOfLessons(),
                CourseFixture.defaultCourseType(),
                List.of("UNKNOWN_TAG")
        );

        when(instructorHelperService.getInstructorOrThrow(CourseFixture.defaultInstructorId()))
                .thenReturn(InstructorResponseFixture.instructorResponse());
        doNothing().when(vehicleHelperService).validateVehicleForUse(CourseFixture.defaultVehicleId());
        when(courseTagRepository.findByCodeIn(any())).thenReturn(Collections.emptyList());

        BusinessException ex = assertThrows(BusinessException.class, () -> courseService.createCourse(request));
        assertEquals(ErrorCode.INVALID_COURSE_TAG.getCode(), ex.getErrorCode());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void whenCreateCourseWithTags_thenReturnsTagCodes() {
        CourseTag intensive = CourseTag.builder().id(1L).code("INTENSIVE").name("Intensive").build();

        CourseRequest request = new CourseRequest(
                CourseFixture.defaultName(),
                CourseFixture.defaultDescription(),
                CourseFixture.defaultPrice(),
                CourseFixture.defaultInstructorId(),
                CourseFixture.defaultVehicleId(),
                CourseFixture.defaultNumberOfLessons(),
                CourseFixture.defaultCourseType(),
                List.of("INTENSIVE")
        );

        when(instructorHelperService.getInstructorOrThrow(CourseFixture.defaultInstructorId()))
                .thenReturn(InstructorResponseFixture.instructorResponse());
        doNothing().when(vehicleHelperService).validateVehicleForUse(CourseFixture.defaultVehicleId());
        when(courseTagRepository.findByCodeIn(any())).thenReturn(List.of(intensive));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course saved = invocation.getArgument(0);
            saved.setId(CourseFixture.defaultCourseId());
            return saved;
        });

        CourseResponse result = courseService.createCourse(request);

        assertEquals(List.of("INTENSIVE"), result.courseTagCodes());
    }

    @Test
    void whenGetCourseById_thenReturnsCourseResponse() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // When
        CourseResponse result = courseService.getCourseById(courseId);

        // Then
        assertNotNull(result);
        assertEquals(courseId, result.id());
    }

    @Test
    void whenGetCourseByIdWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseById(courseId));
    }

    @Test
    void whenGetAllCoursesWithoutFilters_thenReturnsAllCourses() {
        // Given
        Long instructorId = null;
        Long vehicleId = null;
        int expectedCoursesCount = 1;

        List<Course> courses = Collections.singletonList(course);
        when(courseRepository.findAll()).thenReturn(courses);

        // When
        List<CourseResponse> result = courseService.getAllCourses(instructorId, vehicleId);

        // Then
        assertNotNull(result);
        assertEquals(expectedCoursesCount, result.size());
    }

    @Test
    void whenGetAllCoursesWithInstructorFilter_thenReturnsCoursesForInstructor() {
        // Given
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = null;
        int expectedCoursesCount = 1;

        List<Course> courses = Collections.singletonList(course);
        when(instructorHelperService.getInstructorOrThrow(instructorId))
                .thenReturn(InstructorResponseFixture.instructorResponse());
        when(courseRepository.findByInstructorId(instructorId)).thenReturn(courses);

        // When
        List<CourseResponse> result = courseService.getAllCourses(instructorId, vehicleId);

        // Then
        assertNotNull(result);
        assertEquals(expectedCoursesCount, result.size());
    }

    @Test
    void whenGetAllCoursesWithVehicleFilter_thenReturnsCoursesForVehicle() {
        // Given
        Long instructorId = null;
        Long vehicleId = CourseFixture.defaultVehicleId();
        int expectedCoursesCount = 1;

        List<Course> courses = Collections.singletonList(course);
        when(vehicleHelperService.getVehicleOrThrow(vehicleId))
                .thenReturn(VehicleResponseFixture.vehicleResponse());
        when(courseRepository.findByVehicleId(vehicleId)).thenReturn(courses);

        // When
        List<CourseResponse> result = courseService.getAllCourses(instructorId, vehicleId);

        // Then
        assertNotNull(result);
        assertEquals(expectedCoursesCount, result.size());
    }

    @Test
    void whenGetAllCoursesWithBothFilters_thenReturnsCoursesMatchingBoth() {
        // Given
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = CourseFixture.defaultVehicleId();
        int expectedCoursesCount = 1;

        List<Course> courses = Collections.singletonList(course);
        when(instructorHelperService.getInstructorOrThrow(instructorId))
                .thenReturn(InstructorResponseFixture.instructorResponse());
        when(vehicleHelperService.getVehicleOrThrow(vehicleId))
                .thenReturn(VehicleResponseFixture.vehicleResponse());
        when(courseRepository.findByInstructorIdAndVehicleId(instructorId, vehicleId)).thenReturn(courses);

        // When
        List<CourseResponse> result = courseService.getAllCourses(instructorId, vehicleId);

        // Then
        assertNotNull(result);
        assertEquals(expectedCoursesCount, result.size());
    }

    @Test
    void whenUpdateCourse_thenReturnsUpdatedCourseResponse() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = CourseFixture.defaultVehicleId();
        String updatedName = "Advanced Course";
        String updatedDescription = "Advanced course description";
        java.math.BigDecimal updatedPrice = CourseFixture.defaultPrice().multiply(java.math.BigDecimal.valueOf(2));
        Integer updatedNumberOfLessons = 15;

        CourseRequest updateRequest = new CourseRequest(
                updatedName,
                updatedDescription,
                updatedPrice,
                instructorId,
                vehicleId,
                updatedNumberOfLessons,
                Course.CourseType.PRACTICAL,
                null
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CourseResponse result = courseService.updateCourse(courseId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(updatedName, result.name());
        assertEquals(updatedDescription, result.description());
        assertEquals(updatedPrice, result.price());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void whenUpdateCourseWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> courseService.updateCourse(courseId, courseRequest));
    }

    @Test
    void whenUpdateCourseWithNewInstructor_thenValidatesNewInstructor() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        Long newInstructorId = 2L;
        Long vehicleId = CourseFixture.defaultVehicleId();

        CourseRequest updateRequest = CourseFixture.courseRequest(newInstructorId, vehicleId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(instructorHelperService.getInstructorOrThrow(newInstructorId))
                .thenReturn(InstructorResponseFixture.instructorResponse(newInstructorId));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        courseService.updateCourse(courseId, updateRequest);

        // Then
        verify(instructorHelperService, times(1)).getInstructorOrThrow(newInstructorId);
    }

    @Test
    void whenUpdateCourseWithNewVehicle_thenValidatesNewVehicle() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        Long instructorId = CourseFixture.defaultInstructorId();
        Long newVehicleId = 2L;

        CourseRequest updateRequest = CourseFixture.courseRequest(instructorId, newVehicleId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        doNothing().when(vehicleHelperService).validateVehicleForUse(newVehicleId);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        courseService.updateCourse(courseId, updateRequest);

        // Then
        verify(vehicleHelperService, times(1)).validateVehicleForUse(newVehicleId);
    }

    @Test
    void whenDeleteCourse_thenDeletesCourse() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        course.setLessons(Collections.emptyList());
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        doNothing().when(courseRepository).deleteById(courseId);

        // When
        assertDoesNotThrow(() -> courseService.deleteCourse(courseId));

        // Then
        verify(courseRepository, times(1)).deleteById(courseId);
    }

    @Test
    void whenDeleteCourseWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteCourse(courseId));
    }

    @Test
    void whenDeleteCourseWithLessons_thenThrowsBusinessException() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        Long lessonId = LessonFixture.defaultLessonId();

        Lesson lesson = Lesson.builder().id(lessonId).build();
        course.setLessons(Collections.singletonList(lesson));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> courseService.deleteCourse(courseId));

        assertEquals(ErrorCode.COURSE_HAS_LESSONS.getCode(), exception.getErrorCode());
        verify(courseRepository, never()).deleteById(anyLong());
    }

    @Test
    void whenGetCourseLessons_thenReturnsCourseLessons() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        Long instructorId = CourseFixture.defaultInstructorId();
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        int expectedLessonsCount = 1;

        Lesson lesson = LessonFixture.lessonScheduled();
        lesson.setCourse(course);
        course.setLessons(Collections.singletonList(lesson));

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);

        // When
        List<LessonResponse> result = courseService.getCourseLessons(courseId);

        // Then
        assertNotNull(result);
        assertEquals(expectedLessonsCount, result.size());
        assertEquals(instructorName, result.getFirst().instructorName());
    }
}

