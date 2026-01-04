package com.drivingschool.scheduling.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.scheduling.client.PaymentClient;
import com.drivingschool.scheduling.dto.LessonRequest;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.dto.PaymentRequest;
import com.drivingschool.scheduling.dto.PaymentResponse;
import com.drivingschool.scheduling.entity.Course;
import com.drivingschool.scheduling.entity.Lesson;
import com.drivingschool.scheduling.fixture.CourseFixture;
import com.drivingschool.scheduling.fixture.InstructorResponseFixture;
import com.drivingschool.scheduling.fixture.LessonFixture;
import com.drivingschool.scheduling.mapper.SchedulingMapper;
import com.drivingschool.scheduling.repository.CourseRepository;
import com.drivingschool.scheduling.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private InstructorHelperService instructorHelperService;

    @Mock
    private VehicleHelperService vehicleHelperService;

    @Mock
    private StudentHelperService studentHelperService;

    private final SchedulingMapper schedulingMapper = new SchedulingMapper();

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private PaymentClient paymentClient;

    private LessonService lessonService;

    private LessonRequest lessonRequest;
    private Course course;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        lessonRequest = LessonFixture.lessonRequest();
        course = CourseFixture.course();
        lesson = LessonFixture.lessonScheduled();

        lessonService = new LessonService(
                lessonRepository,
                courseRepository,
                instructorHelperService,
                vehicleHelperService,
                studentHelperService,
                schedulingMapper,
                kafkaTemplate,
                paymentClient
        );
    }

    @Test
    void testBookLesson_Success() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        Long studentId = LessonFixture.defaultStudentId();
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = CourseFixture.defaultVehicleId();
        Long lessonId = LessonFixture.defaultLessonId();
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        String kafkaTopic = "lesson-booked";
        BigDecimal paymentAmount = new BigDecimal("100.00");
        Long paymentId = 1L;

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        doNothing().when(studentHelperService).validateStudentForAction(studentId);
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);
        doNothing().when(vehicleHelperService).validateVehicleForUse(vehicleId);
        when(lessonRepository.findConflictingLessons(
                instructorId,
                lessonRequest.startTime(),
                lessonRequest.endTime()))
                .thenReturn(Collections.emptyList());
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson saved = invocation.getArgument(0);
            saved.setId(lessonId);
            return saved;
        });
        when(paymentClient.createPendingPayment(any(PaymentRequest.class)))
                .thenReturn(ApiResult.success(new PaymentResponse(paymentId, studentId, paymentAmount, "PENDING", lessonId, LocalDateTime.now())));

        // When
        LessonResponse result = lessonService.bookLesson(lessonRequest);

        // Then
        assertNotNull(result);
        assertEquals(lessonId, result.id());
        assertEquals(instructorName, result.instructorName());
        verify(lessonRepository, times(1)).save(any(Lesson.class));
        verify(kafkaTemplate, times(1)).send(eq(kafkaTopic), anyString(), any(Lesson.class));
    }

    @Test
    void testBookLesson_CourseNotFound() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> lessonService.bookLesson(lessonRequest));
    }

    @Test
    void testBookLesson_StudentNotActive() {
        // Given
        Long studentId = LessonFixture.defaultStudentId();
        String expectedErrorCode = "STUDENT_NOT_ACTIVE";

        doThrow(new BusinessException("Student not active", expectedErrorCode))
                .when(studentHelperService).validateStudentForAction(studentId);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> lessonService.bookLesson(lessonRequest));

        assertEquals(expectedErrorCode, exception.getErrorCode());
    }

    @Test
    void testBookLesson_PastTime() {
        // Given
        Long studentId = LessonFixture.defaultStudentId();
        Long courseId = CourseFixture.defaultCourseId();
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = CourseFixture.defaultVehicleId();
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        String expectedErrorCode = "INVALID_TIME";
        int daysInPast = 1;

        LocalDateTime pastTime = LocalDateTime.now().minusDays(daysInPast);
        LessonRequest pastRequest = LessonFixture.lessonRequest(studentId, courseId, pastTime);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        doNothing().when(studentHelperService).validateStudentForAction(studentId);
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);
        doNothing().when(vehicleHelperService).validateVehicleForUse(vehicleId);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> lessonService.bookLesson(pastRequest));

        assertEquals(expectedErrorCode, exception.getErrorCode());
    }

    @Test
    void testBookLesson_SchedulingConflict() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        Long studentId = LessonFixture.defaultStudentId();
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = CourseFixture.defaultVehicleId();
        Long conflictingLessonId = 2L;
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        String expectedErrorCode = "SCHEDULING_CONFLICT";

        Lesson conflictingLesson = LessonFixture.lesson(conflictingLessonId, Lesson.LessonStatus.SCHEDULED);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        doNothing().when(studentHelperService).validateStudentForAction(studentId);
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);
        doNothing().when(vehicleHelperService).validateVehicleForUse(vehicleId);
        when(lessonRepository.findConflictingLessons(
                instructorId,
                lessonRequest.startTime(),
                lessonRequest.endTime()))
                .thenReturn(Collections.singletonList(conflictingLesson));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> lessonService.bookLesson(lessonRequest));

        assertEquals(expectedErrorCode, exception.getErrorCode());
    }

    @Test
    void testBookLesson_CalculatesEndTime() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        Long studentId = LessonFixture.defaultStudentId();
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = CourseFixture.defaultVehicleId();
        Long lessonId = LessonFixture.defaultLessonId();
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        BigDecimal paymentAmount = new BigDecimal("100.00");
        Long paymentId = 1L;

        LessonRequest requestWithoutEndTime = LessonFixture.lessonRequestWithoutEndTime();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        doNothing().when(studentHelperService).validateStudentForAction(studentId);
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);
        doNothing().when(vehicleHelperService).validateVehicleForUse(vehicleId);
        when(lessonRepository.findConflictingLessons(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson saved = invocation.getArgument(0);
            saved.setId(lessonId);
            return saved;
        });
        when(paymentClient.createPendingPayment(any())).thenReturn(ApiResult.success(new PaymentResponse(paymentId, studentId, paymentAmount, "PENDING", lessonId, LocalDateTime.now())));

        // When
        lessonService.bookLesson(requestWithoutEndTime);

        // Then
        verify(lessonRepository, times(1)).save(any(Lesson.class));
    }

    @Test
    void testBookLesson_ExtraLessonPricing() {
        // Given - Student has already booked 10 lessons (course limit)
        Long courseId = CourseFixture.defaultCourseId();
        Long studentId = LessonFixture.defaultStudentId();
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = CourseFixture.defaultVehicleId();
        Long lessonId = LessonFixture.defaultLessonId();
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        int existingLessonsCount = 10;
        BigDecimal priceMultiplier = BigDecimal.valueOf(2);

        course.getLessons().addAll(Collections.nCopies(existingLessonsCount, lesson));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        doNothing().when(studentHelperService).validateStudentForAction(studentId);
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);
        doNothing().when(vehicleHelperService).validateVehicleForUse(vehicleId);
        when(lessonRepository.findConflictingLessons(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson saved = invocation.getArgument(0);
            saved.setId(lessonId);
            return saved;
        });
        when(paymentClient.createPendingPayment(any())).thenReturn(ApiResult.success(new PaymentResponse(1L, studentId, course.getPricePerLesson().multiply(priceMultiplier), "PENDING", lessonId, LocalDateTime.now())));

        // When
        lessonService.bookLesson(lessonRequest);

        // Then - Verify extra lesson pricing (2x) is used
        verify(paymentClient).createPendingPayment(argThat(request -> {
            BigDecimal expectedPrice = course.getPricePerLesson().multiply(priceMultiplier);
            return request.amount().compareTo(expectedPrice) == 0;
        }));
    }

    @Test
    void testGetLessonById_Success() {
        // Given
        Long lessonId = LessonFixture.defaultLessonId();
        Long instructorId = CourseFixture.defaultInstructorId();
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);

        // When
        LessonResponse result = lessonService.getLessonById(lessonId);

        // Then
        assertNotNull(result);
        assertEquals(lessonId, result.id());
        assertEquals(instructorName, result.instructorName());
    }

    @Test
    void testGetLessonById_NotFound() {
        // Given
        Long lessonId = LessonFixture.defaultLessonId();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> lessonService.getLessonById(lessonId));
    }

    @Test
    void testGetLessonById_NoCourse() {
        // Given
        Long lessonId = LessonFixture.defaultLessonId();
        String expectedErrorCode = "LESSON_WITHOUT_COURSE";

        Lesson lessonWithoutCourse = LessonFixture.lesson();
        lessonWithoutCourse.setCourse(null);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lessonWithoutCourse));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> lessonService.getLessonById(lessonId));

        assertEquals(expectedErrorCode, exception.getErrorCode());
    }

    @Test
    void testUpdateLesson_Success() {
        // Given
        Long lessonId = LessonFixture.defaultLessonId();
        Long courseId = CourseFixture.defaultCourseId();
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = CourseFixture.defaultVehicleId();
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        String kafkaTopic = "lesson-updated";

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);
        doNothing().when(vehicleHelperService).validateVehicleForUse(vehicleId);
        when(lessonRepository.findConflictingLessons(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        LessonResponse result = lessonService.updateLesson(lessonId, lessonRequest);

        // Then
        assertNotNull(result);
        assertEquals(instructorName, result.instructorName());
        verify(lessonRepository, times(1)).save(any(Lesson.class));
        verify(kafkaTemplate, times(1)).send(eq(kafkaTopic), anyString(), any(Lesson.class));
    }

    @Test
    void testUpdateLesson_NotFound() {
        // Given
        Long lessonId = LessonFixture.defaultLessonId();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> lessonService.updateLesson(lessonId, lessonRequest));
    }

    @Test
    void testCancelLesson_Success() {
        // Given
        Long lessonId = LessonFixture.defaultLessonId();
        Lesson.LessonStatus expectedStatus = Lesson.LessonStatus.CANCELLED;
        String kafkaTopic = "lesson-cancelled";

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson saved = invocation.getArgument(0);
            saved.setStatus(expectedStatus);
            return saved;
        });

        // When
        assertDoesNotThrow(() -> lessonService.cancelLesson(lessonId));

        // Then
        verify(lessonRepository, times(1)).save(any(Lesson.class));
        verify(kafkaTemplate, times(1)).send(eq(kafkaTopic), anyString(), any(Lesson.class));
    }

    @Test
    void testCancelLesson_NotFound() {
        // Given
        Long lessonId = LessonFixture.defaultLessonId();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> lessonService.cancelLesson(lessonId));
    }

    @Test
    void testIsInstructorAvailable_True() {
        // Given
        Long instructorId = CourseFixture.defaultInstructorId();
        LocalDateTime startTime = LessonFixture.defaultStartTime();
        LocalDateTime endTime = startTime.plusHours(2);

        when(instructorHelperService.getInstructorOrThrow(instructorId))
                .thenReturn(InstructorResponseFixture.instructorResponse());
        when(lessonRepository.findConflictingLessons(instructorId, startTime, endTime))
                .thenReturn(Collections.emptyList());

        // When
        Boolean result = lessonService.isInstructorAvailable(instructorId, startTime, endTime);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsInstructorAvailable_False() {
        // Given
        Long instructorId = CourseFixture.defaultInstructorId();
        LocalDateTime startTime = LessonFixture.defaultStartTime();
        LocalDateTime endTime = startTime.plusHours(2);

        when(instructorHelperService.getInstructorOrThrow(instructorId))
                .thenReturn(InstructorResponseFixture.instructorResponse());
        when(lessonRepository.findConflictingLessons(instructorId, startTime, endTime))
                .thenReturn(Collections.singletonList(lesson));

        // When
        Boolean result = lessonService.isInstructorAvailable(instructorId, startTime, endTime);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsVehicleAvailable_True() {
        // Given
        Long vehicleId = CourseFixture.defaultVehicleId();
        LocalDateTime startTime = LessonFixture.defaultStartTime();
        LocalDateTime endTime = startTime.plusHours(2);

        when(lessonRepository.findConflictingLessonsForVehicle(vehicleId, startTime, endTime))
                .thenReturn(Collections.emptyList());

        // When
        Boolean result = lessonService.isVehicleAvailable(vehicleId, startTime, endTime);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsVehicleAvailable_False() {
        // Given
        Long vehicleId = CourseFixture.defaultVehicleId();
        LocalDateTime startTime = LessonFixture.defaultStartTime();
        LocalDateTime endTime = startTime.plusHours(2);

        when(lessonRepository.findConflictingLessonsForVehicle(vehicleId, startTime, endTime))
                .thenReturn(Collections.singletonList(lesson));

        // When
        Boolean result = lessonService.isVehicleAvailable(vehicleId, startTime, endTime);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetStudentLessons_WithStatus() {
        // Given
        Long studentId = LessonFixture.defaultStudentId();
        Long instructorId = CourseFixture.defaultInstructorId();
        Lesson.LessonStatus status = Lesson.LessonStatus.SCHEDULED;
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        int expectedLessonsCount = 1;

        when(lessonRepository.findByStudentIdAndStatus(studentId, status))
                .thenReturn(Collections.singletonList(lesson));
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);

        // When
        List<LessonResponse> result = lessonService.getStudentLessons(studentId, status);

        // Then
        assertNotNull(result);
        assertEquals(expectedLessonsCount, result.size());
        assertEquals(instructorName, result.getFirst().instructorName());
    }

    @Test
    void testGetStudentLessons_WithoutStatus() {
        // Given
        Long studentId = LessonFixture.defaultStudentId();
        Long instructorId = CourseFixture.defaultInstructorId();
        Lesson.LessonStatus status = null;
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        int expectedLessonsCount = 1;

        when(lessonRepository.findByStudentId(studentId)).thenReturn(Collections.singletonList(lesson));
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);

        // When
        List<LessonResponse> result = lessonService.getStudentLessons(studentId, status);

        // Then
        assertNotNull(result);
        assertEquals(expectedLessonsCount, result.size());
        assertEquals(instructorName, result.getFirst().instructorName());
    }

    @Test
    void testGetInstructorLessons() {
        // Given
        Long instructorId = CourseFixture.defaultInstructorId();
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        int expectedLessonsCount = 1;

        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);
        when(lessonRepository.findByInstructorId(instructorId)).thenReturn(Collections.singletonList(lesson));

        // When
        List<LessonResponse> result = lessonService.getInstructorLessons(instructorId);

        // Then
        assertNotNull(result);
        assertEquals(expectedLessonsCount, result.size());
        assertEquals(instructorName, result.getFirst().instructorName());
    }

    @Test
    void testGetLessonsByDateRange() {
        // Given
        Long instructorId = CourseFixture.defaultInstructorId();
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusDays(7);
        int expectedLessonsCount = 1;

        when(lessonRepository.findByDateRange(startTime, endTime))
                .thenReturn(Collections.singletonList(lesson));
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);

        // When
        List<LessonResponse> result = lessonService.getLessonsByDateRange(startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(expectedLessonsCount, result.size());
        assertEquals(instructorName, result.getFirst().instructorName());
    }

    @Test
    void testGetUpcomingLessonsByStudent() {
        // Given
        Long studentId = LessonFixture.defaultStudentId();
        Long instructorId = CourseFixture.defaultInstructorId();
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        int expectedLessonsCount = 1;

        when(lessonRepository.findUpcomingByStudentId(eq(studentId), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(lesson));
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);

        // When
        List<LessonResponse> result = lessonService.getUpcomingLessonsByStudent(studentId);

        // Then
        assertNotNull(result);
        assertEquals(expectedLessonsCount, result.size());
        assertEquals(instructorName, result.getFirst().instructorName());
    }

    @Test
    void testGetLessonsByCourse() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        Long instructorId = CourseFixture.defaultInstructorId();
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        int expectedLessonsCount = 1;

        when(lessonRepository.findByCourseId(courseId)).thenReturn(Collections.singletonList(lesson));
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);

        // When
        List<LessonResponse> result = lessonService.getLessonsByCourse(courseId);

        // Then
        assertNotNull(result);
        assertEquals(expectedLessonsCount, result.size());
        assertEquals(instructorName, result.getFirst().instructorName());
    }

    @Test
    void testBookLesson_PaymentCreationFailure() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        Long studentId = LessonFixture.defaultStudentId();
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = CourseFixture.defaultVehicleId();
        Long lessonId = LessonFixture.defaultLessonId();
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        String errorMessage = "Payment service unavailable";

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        doNothing().when(studentHelperService).validateStudentForAction(studentId);
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);
        doNothing().when(vehicleHelperService).validateVehicleForUse(vehicleId);
        when(lessonRepository.findConflictingLessons(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson saved = invocation.getArgument(0);
            saved.setId(lessonId);
            return saved;
        });
        when(paymentClient.createPendingPayment(any())).thenThrow(new RuntimeException(errorMessage));

        // When - Should not throw exception even if payment creation fails
        assertDoesNotThrow(() -> {
            lessonService.bookLesson(lessonRequest);
        });

        // Then
        verify(lessonRepository, times(1)).save(any(Lesson.class));
    }
}

