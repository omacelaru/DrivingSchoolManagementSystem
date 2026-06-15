package com.drivingschool.scheduling.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.scheduling.dto.LessonRequest;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.dto.LessonPaymentSyncResponse;
import com.drivingschool.scheduling.dto.PaymentRequest;
import com.drivingschool.scheduling.dto.PaymentResponse;
import com.drivingschool.scheduling.entity.Course;
import com.drivingschool.scheduling.entity.Lesson;
import com.drivingschool.scheduling.mapper.SchedulingMapper;
import com.drivingschool.scheduling.repository.CourseRepository;
import com.drivingschool.scheduling.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LessonService {
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final InstructorHelperService instructorHelperService;
    private final VehicleHelperService vehicleHelperService;
    private final StudentHelperService studentHelperService;
    private final SchedulingMapper schedulingMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentHelperService paymentClient;

    public LessonResponse bookLesson(LessonRequest request, Long studentId) {
        log.info("Booking lesson for student ID: {}", studentId);

        studentHelperService.validateStudentForAction(studentId);

        Course course = loadCourse(request.courseId());
        LessonPriceInfo priceInfo = calculateLessonPrice(course, studentId);
        validateResources(course);

        LocalDateTime endTime = calculateEndTime(request);
        validateTimeConstraints(request.startTime(), endTime);
        validateNoSchedulingConflicts(course.getInstructorId(), request.startTime(), endTime);

        Lesson lesson = createAndSaveLesson(request, course, endTime, studentId);

        createPendingPaymentForLesson(lesson, priceInfo, course);

        publishLessonBookedEvent(lesson);

        String instructorName = instructorHelperService.getInstructorName(course.getInstructorId());
        return schedulingMapper.toResponse(lesson, instructorName);
    }

    private Course loadCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
    }

    private LessonPriceInfo calculateLessonPrice(Course course, Long studentId) {
        long bookedCount = course.getBookedLessonsCountForStudent(studentId);
        boolean isExtraLesson = bookedCount >= course.getNumberOfLessons();
        BigDecimal lessonPrice = isExtraLesson
                ? course.getPricePerLesson().multiply(BigDecimal.valueOf(2))
                : course.getPricePerLesson();

        if (isExtraLesson) {
            log.info("Student {} booking extra lesson for course {}. Extra lesson price (2x): {}",
                    studentId, course.getId(), lessonPrice);
        } else {
            log.info("Student {} booking lesson {}/{} from course {}. Price per lesson: {}",
                    studentId, bookedCount + 1, course.getNumberOfLessons(), course.getId(), lessonPrice);
        }

        return new LessonPriceInfo(lessonPrice, isExtraLesson);
    }

    private void validateResources(Course course) {
        instructorHelperService.getInstructorName(course.getInstructorId());
        vehicleHelperService.validateVehicleForUse(course.getVehicleId());
        log.debug("Instructor ID {} and Vehicle ID {} validated", course.getInstructorId(), course.getVehicleId());
    }

    private LocalDateTime calculateEndTime(LessonRequest request) {
        if (request.endTime() != null) {
            return request.endTime();
        }
        LocalDateTime endTime = request.startTime().plus(Duration.ofHours(1).plusMinutes(30));
        log.debug("EndTime not provided, calculated as startTime + 1h30: {}", endTime);
        return endTime;
    }

    private void validateTimeConstraints(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot book lessons in the past", ErrorCode.INVALID_TIME);
        }

        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new BusinessException("End time must be after start time", ErrorCode.INVALID_TIME_RANGE);
        }
    }

    private void validateNoSchedulingConflicts(Long instructorId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Lesson> conflicts = lessonRepository.findConflictingLessons(instructorId, startTime, endTime);
        if (!conflicts.isEmpty()) {
            throw new BusinessException("Instructor is not available at the requested time", ErrorCode.SCHEDULING_CONFLICT);
        }
    }

    private Lesson createAndSaveLesson(LessonRequest request, Course course, LocalDateTime endTime, Long studentId) {
        Lesson lesson = schedulingMapper.toEntity(request, course, endTime);
        lesson.setStudentId(studentId);
        // Ensure endTime is always present even when request.endTime is omitted.
        lesson.setEndTime(endTime);
        return lessonRepository.save(lesson);
    }

    private void createPendingPaymentForLesson(Lesson lesson, LessonPriceInfo priceInfo, Course course) {
        log.info("Creating pending payment for lesson ID: {}, amount: {}", lesson.getId(), priceInfo.price());
        try {
            PaymentRequest paymentRequest = buildPaymentRequest(lesson, priceInfo, course);
            ApiResult<PaymentResponse> paymentResult = paymentClient.createPendingPayment(paymentRequest);

            if (paymentResult.success() && paymentResult.data() != null) {
                log.info("Pending payment created with ID: {} for lesson ID: {}, amount: {}",
                        paymentResult.data().id(), lesson.getId(), priceInfo.price());
            }
        } catch (Exception e) {
            log.error("Failed to create pending payment for lesson ID: {}", lesson.getId(), e);
            // Don't fail the lesson booking if payment creation fails
        }
    }

    private PaymentRequest buildPaymentRequest(Lesson lesson, LessonPriceInfo priceInfo, Course course) {
        String description = priceInfo.isExtraLesson()
                ? "Payment for extra lesson (beyond course limit) - Course: " + course.getName()
                : "Payment for lesson from course: " + course.getName();

        return new PaymentRequest(
                lesson.getStudentId(),
                priceInfo.price(),
                lesson.getId(),
                description
        );
    }

    private void publishLessonBookedEvent(Lesson lesson) {
        kafkaTemplate.send("lesson-booked", lesson.getId().toString(), lesson);
        log.info("Lesson booked with ID: {}", lesson.getId());
    }

    private record LessonPriceInfo(BigDecimal price, boolean isExtraLesson) {
    }

    public List<LessonResponse> getInstructorLessons(Long instructorId) {
        log.info("Fetching lessons for instructor ID: {}", instructorId);
        String instructorName = instructorHelperService.getInstructorName(instructorId);
        List<Lesson> lessons = lessonRepository.findByInstructorId(instructorId);
        return mapLessonsToResponse(lessons, instructorName);
    }

    private List<LessonResponse> mapLessonsToResponse(List<Lesson> lessons, String instructorName) {
        return lessons.stream()
                .map(lesson -> schedulingMapper.toResponse(lesson, instructorName))
                .collect(Collectors.toList());
    }

    public LessonResponse getLessonById(Long id) {
        log.info("Fetching lesson with ID: {}", id);
        Lesson lesson = findLessonById(id);
        Course course = validateLessonHasCourse(lesson);
        String instructorName = instructorHelperService.getInstructorName(course.getInstructorId());
        return schedulingMapper.toResponse(lesson, instructorName);
    }

    private Lesson findLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));
    }

    private Course validateLessonHasCourse(Lesson lesson) {
        Course course = lesson.getCourse();
        if (course == null) {
            throw new BusinessException("Lesson must be associated with a course", ErrorCode.LESSON_WITHOUT_COURSE);
        }
        return course;
    }

    public LessonResponse updateLesson(Long id, LessonRequest request, Long studentId) {
        log.info("Updating lesson with ID: {}", id);
        Lesson lesson = findLessonById(id);
        validateStudentIfChanged(lesson, studentId);
        Course course = loadCourseForUpdate(lesson, request);
        validateResources(course);
        LocalDateTime endTime = calculateEndTime(request);
        validateNoSchedulingConflictsForUpdate(course.getInstructorId(), request.startTime(), endTime, id);

        schedulingMapper.updateEntity(lesson, request, course, endTime);
        lesson.setStudentId(studentId);
        // Keep entity consistent with computed fallback duration.
        lesson.setEndTime(endTime);
        lesson = lessonRepository.save(lesson);
        publishLessonUpdatedEvent(lesson);

        String instructorName = instructorHelperService.getInstructorName(course.getInstructorId());
        return schedulingMapper.toResponse(lesson, instructorName);
    }

    private void validateStudentIfChanged(Lesson lesson, Long studentId) {
        if (!lesson.getStudentId().equals(studentId)) {
            studentHelperService.validateStudentForAction(studentId);
            log.debug("Student ID {} validated for action", studentId);
        }
    }

    private Course loadCourseForUpdate(Lesson lesson, LessonRequest request) {
        if (request.courseId() != null) {
            return courseRepository.findById(request.courseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course", request.courseId()));
        }

        Course course = lesson.getCourse();
        if (course == null) {
            throw new BusinessException("Course ID is required for updating lesson", ErrorCode.MISSING_COURSE_ID);
        }
        return course;
    }

    private void validateNoSchedulingConflictsForUpdate(Long instructorId, LocalDateTime startTime, LocalDateTime endTime, Long lessonId) {
        List<Lesson> conflicts = lessonRepository.findConflictingLessons(instructorId, startTime, endTime);
        conflicts.removeIf(l -> l.getId().equals(lessonId));
        if (!conflicts.isEmpty()) {
            throw new BusinessException("Instructor is not available at the requested time", ErrorCode.SCHEDULING_CONFLICT);
        }
    }

    private void publishLessonUpdatedEvent(Lesson lesson) {
        kafkaTemplate.send("lesson-updated", lesson.getId().toString(), lesson);
        log.info("Lesson updated with ID: {}", lesson.getId());
    }

    public void cancelLesson(Long id) {
        log.info("Cancelling lesson with ID: {}", id);
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        lesson.setStatus(Lesson.LessonStatus.CANCELLED);
        lessonRepository.save(lesson);
        reconcilePaymentsForCancelledLesson(lesson);

        kafkaTemplate.send("lesson-cancelled", lesson.getId().toString(), lesson);
        log.info("Lesson cancelled with ID: {}", id);
    }

    private void reconcilePaymentsForCancelledLesson(Lesson lesson) {
        try {
            ApiResult<LessonPaymentSyncResponse> syncResult = paymentClient.reconcilePaymentsForCancelledLesson(
                    lesson.getId(),
                    lesson.getStudentId()
            );
            if (syncResult.success() && syncResult.data() != null) {
                log.info("Payment reconciliation for cancelled lesson {} completed. Cancelled: {}, Refunded: {}",
                        lesson.getId(),
                        syncResult.data().cancelledCount(),
                        syncResult.data().refundedCount());
            }
        } catch (Exception ex) {
            // Lesson cancellation remains primary action; payment reconciliation is best-effort.
            log.warn("Failed to reconcile payments for cancelled lesson ID: {}", lesson.getId(), ex);
        }
    }

    @Transactional(readOnly = true)
    public Boolean isInstructorAvailable(Long instructorId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Checking availability for instructor ID: {} between {} and {}", instructorId, startTime, endTime);

        // Validate instructor exists
        instructorHelperService.getInstructorOrThrow(instructorId);

        List<Lesson> conflicts = lessonRepository.findConflictingLessons(instructorId, startTime, endTime);
        return conflicts.isEmpty();
    }

    @Transactional(readOnly = true)
    public Boolean isVehicleAvailable(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Checking availability for vehicle ID: {} between {} and {}", vehicleId, startTime, endTime);
        List<Lesson> conflicts = lessonRepository.findConflictingLessonsForVehicle(vehicleId, startTime, endTime);
        boolean isAvailable = conflicts.isEmpty();
        log.debug("Vehicle ID: {} is available: {}", vehicleId, isAvailable);
        return isAvailable;
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getStudentLessons(Long studentId, Lesson.LessonStatus status) {
        log.info("Fetching lessons for student ID: {} with status: {}", studentId, status);
        List<Lesson> lessons = findLessonsByStudentAndStatus(studentId, status);
        return mapLessonsToResponseWithCourse(lessons);
    }

    private List<Lesson> findLessonsByStudentAndStatus(Long studentId, Lesson.LessonStatus status) {
        return status != null
                ? lessonRepository.findByStudentIdAndStatus(studentId, status)
                : lessonRepository.findByStudentId(studentId);
    }

    private List<LessonResponse> mapLessonsToResponseWithCourse(List<Lesson> lessons) {
        return lessons.stream()
                .map(this::mapLessonToResponseWithCourse)
                .collect(Collectors.toList());
    }

    private LessonResponse mapLessonToResponseWithCourse(Lesson lesson) {
        Course course = validateLessonHasCourse(lesson);
        String instructorName = instructorHelperService.getInstructorName(course.getInstructorId());
        return schedulingMapper.toResponse(lesson, instructorName);
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Fetching lessons between {} and {}", startTime, endTime);
        List<Lesson> lessons = lessonRepository.findByDateRange(startTime, endTime);
        return mapLessonsToResponseWithCourse(lessons);
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getUpcomingLessonsByStudent(Long studentId) {
        log.info("Fetching upcoming lessons for student ID: {}", studentId);
        List<Lesson> lessons = lessonRepository.findUpcomingByStudentId(studentId, LocalDateTime.now());
        return mapLessonsToResponseWithCourse(lessons);
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsByCourse(Long courseId) {
        log.info("Fetching lessons for course ID: {}", courseId);
        List<Lesson> lessons = lessonRepository.findByCourseId(courseId);
        return mapLessonsToResponseWithCourse(lessons);
    }
}

