package com.drivingschool.scheduling.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
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
    void whenBookLesson_thenReturnsLessonResponse() {
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
    void whenBookLessonWithNonExistentCourseId_thenThrowsResourceNotFoundException() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> lessonService.bookLesson(lessonRequest));
    }

    @Test
    void whenBookLessonWithInactiveStudent_thenThrowsBusinessException() {
        // Given
        Long studentId = LessonFixture.defaultStudentId();

        doThrow(new BusinessException("Student not active", ErrorCode.STUDENT_NOT_ACTIVE))
                .when(studentHelperService).validateStudentForAction(studentId);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> lessonService.bookLesson(lessonRequest));

        assertEquals(ErrorCode.STUDENT_NOT_ACTIVE.getCode(), exception.getErrorCode());
    }

    @Test
    void whenBookLessonWithPastTime_thenThrowsBusinessException() {
        // Given
        Long studentId = LessonFixture.defaultStudentId();
        Long courseId = CourseFixture.defaultCourseId();
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = CourseFixture.defaultVehicleId();
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();
        int daysInPast = 1;

        LocalDateTime pastTime = LocalDateTime.now().minusDays(daysInPast);
        LessonRequest pastRequest = LessonFixture.lessonRequest(studentId, courseId, pastTime);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        doNothing().when(studentHelperService).validateStudentForAction(studentId);
        when(instructorHelperService.getInstructorName(instructorId)).thenReturn(instructorName);
        doNothing().when(vehicleHelperService).validateVehicleForUse(vehicleId);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> lessonService.bookLesson(pastRequest));

        assertEquals(ErrorCode.INVALID_TIME.getCode(), exception.getErrorCode());
    }

    @Test
    void whenBookLessonWithSchedulingConflict_thenThrowsBusinessException() {
        // Given
        Long courseId = CourseFixture.defaultCourseId();
        Long studentId = LessonFixture.defaultStudentId();
        Long instructorId = CourseFixture.defaultInstructorId();
        Long vehicleId = CourseFixture.defaultVehicleId();
        Long conflictingLessonId = 2L;
        String instructorName = InstructorResponseFixture.defaultFirstName() + " " + InstructorResponseFixture.defaultLastName();

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

        assertEquals(ErrorCode.SCHEDULING_CONFLICT.getCode(), exception.getErrorCode());
    }

    @Test
    void whenBookLessonWithoutEndTime_thenCalculatesEndTime() {
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
    void whenBookLessonBeyondCourseLimit_thenAppliesExtraLessonPricing() {
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
    void whenGetLessonById_thenReturnsLessonResponse() {
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
    void whenGetLessonByIdWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        Long lessonId = LessonFixture.defaultLessonId();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> lessonService.getLessonById(lessonId));
    }

    @Test
    void whenGetLessonByIdWithoutCourse_thenThrowsBusinessException() {
        // Given
        Long lessonId = LessonFixture.defaultLessonId();

        Lesson lessonWithoutCourse = LessonFixture.lesson();
        lessonWithoutCourse.setCourse(null);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lessonWithoutCourse));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> lessonService.getLessonById(lessonId));

        assertEquals(ErrorCode.LESSON_WITHOUT_COURSE.getCode(), exception.getErrorCode());
    }

    @Test
    void whenUpdateLesson_thenReturnsUpdatedLessonResponse() {
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
    void whenUpdateLessonWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        Long lessonId = LessonFixture.defaultLessonId();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> lessonService.updateLesson(lessonId, lessonRequest));
    }

    @Test
    void whenCancelLesson_thenCancelsLesson() {
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
    void whenCancelLessonWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        Long lessonId = LessonFixture.defaultLessonId();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> lessonService.cancelLesson(lessonId));
    }

    @Test
    void whenIsInstructorAvailable_thenReturnsTrue() {
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
    void whenIsInstructorAvailableWithConflicts_thenReturnsFalse() {
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
    void whenIsVehicleAvailable_thenReturnsTrue() {
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
    void whenIsVehicleAvailableWithConflicts_thenReturnsFalse() {
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
    void whenGetStudentLessonsWithStatus_thenReturnsLessonsWithStatus() {
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
    void whenGetStudentLessonsWithoutStatus_thenReturnsAllStudentLessons() {
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
    void whenGetInstructorLessons_thenReturnsInstructorLessons() {
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
    void whenGetLessonsByDateRange_thenReturnsLessonsInRange() {
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
    void whenGetUpcomingLessonsByStudent_thenReturnsUpcomingLessons() {
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
    void whenGetLessonsByCourse_thenReturnsCourseLessons() {
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
    void whenBookLessonAndPaymentCreationFails_thenDoesNotThrowException() {
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

