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
import com.drivingschool.scheduling.mapper.SchedulingMapper;
import com.drivingschool.scheduling.repository.CourseRepository;
import com.drivingschool.scheduling.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class SchedulingService {
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final InstructorHelperService instructorHelperService;
    private final VehicleHelperService vehicleHelperService;
    private final StudentHelperService studentHelperService;
    private final SchedulingMapper schedulingMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentClient paymentClient;

    @Value("${lesson.standard-price}")
    private BigDecimal standardLessonPrice;

    //todo prea mare metoda
    public LessonResponse bookLesson(LessonRequest request) {
        log.info("Booking lesson for student ID: {}", request.studentId());

        // Validate student exists and is active
        studentHelperService.validateStudentForAction(request.studentId());
        log.debug("Student ID {} validated for action", request.studentId());

        // Load course if courseId is provided
        Course course = null;
        Long instructorId;
        Long vehicleId;
        boolean isExtraLesson = false;
        BigDecimal lessonPrice = standardLessonPrice;


        // Course provided - get instructorId, vehicleId, and type from course
        course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.courseId()));

        instructorId = course.getInstructorId();
        vehicleId = course.getVehicleId();

        // Force load lessons to check count
        course.getLessons().size();

        // Check how many lessons the student has already booked for this course
        long bookedCount = course.getBookedLessonsCountForStudent(request.studentId());

        if (bookedCount < course.getNumberOfLessons()) {
            // Student is booking a lesson within the course - price is course price / number of lessons
            lessonPrice = course.getPricePerLesson();
            log.info("Student {} booking lesson {}/{} from course {}. Price per lesson: {}",
                    request.studentId(), bookedCount + 1, course.getNumberOfLessons(),
                    request.courseId(), lessonPrice);
        } else {
            // Student is booking an extra lesson beyond the course - double the price per lesson
            isExtraLesson = true;
            lessonPrice = course.getPricePerLesson().multiply(BigDecimal.valueOf(2));
            log.info("Student {} booking extra lesson for course {}. Extra lesson price (2x): {}",
                    request.studentId(), request.courseId(), lessonPrice);
        }

        // Validate instructor exists (this also gets the name)
        String instructorName = instructorHelperService.getInstructorName(instructorId);
        log.debug("Instructor ID {} validated", instructorId);

        // Validate vehicle exists and is available for use
        vehicleHelperService.validateVehicleForUse(vehicleId);
        log.debug("Vehicle ID {} validated for use", vehicleId);

        // Calculate endTime if not provided (default: startTime + 1h30)
        LocalDateTime endTime = request.endTime();
        if (endTime == null) {
            endTime = request.startTime().plus(Duration.ofHours(1).plusMinutes(30));
            log.debug("EndTime not provided, calculated as startTime + 1h30: {}", endTime);
        }

        // Check for conflicts
        List<Lesson> conflicts = lessonRepository.findConflictingLessons(
                instructorId,
                request.startTime(),
                endTime);

        if (!conflicts.isEmpty()) {
            throw new BusinessException("Instructor is not available at the requested time", "SCHEDULING_CONFLICT");
        }

        if (request.startTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot book lessons in the past", "INVALID_TIME");
        }

        if (endTime.isBefore(request.startTime()) || endTime.isEqual(request.startTime())) {
            throw new BusinessException("End time must be after start time", "INVALID_TIME_RANGE");
        }

        Lesson lesson = schedulingMapper.toEntity(request, course, endTime);
        lesson = lessonRepository.save(lesson);

        // Create pending payment for the lesson
        log.info("Creating pending payment for lesson ID: {}, amount: {}", lesson.getId(), lessonPrice);
        try {
            PaymentRequest paymentRequest = new PaymentRequest(
                    request.studentId(),
                    lessonPrice,
                    lesson.getId(),
                    isExtraLesson 
                        ? "Payment for extra lesson (beyond course limit) - Course: " + course.getName()
                        : request.courseId() != null
                            ? "Payment for lesson from course: " + course.getName()
                            : "Payment for additional lesson"
            );

            ApiResult<PaymentResponse> paymentResult = paymentClient.createPendingPayment(paymentRequest);
            if (paymentResult.success() && paymentResult.data() != null) {
                lesson.setPaymentId(paymentResult.data().id());
                lesson = lessonRepository.save(lesson);
                log.info("Pending payment created with ID: {} for lesson ID: {}, amount: {}",
                        paymentResult.data().id(), lesson.getId(), lessonPrice);
            }
        } catch (Exception e) {
            log.error("Failed to create pending payment for lesson ID: {}", lesson.getId(), e);
            // Don't fail the lesson booking if payment creation fails
            // The payment can be created later
        }

        // Publish event to Kafka
        kafkaTemplate.send("lesson-booked", lesson.getId().toString(), lesson);
        log.info("Lesson booked with ID: {}", lesson.getId());

        return schedulingMapper.toResponse(lesson, instructorName);
    }

    public List<LessonResponse> getInstructorLessons(Long instructorId) {
        log.info("Fetching lessons for instructor ID: {}", instructorId);

        String instructorName = instructorHelperService.getInstructorName(instructorId);

        List<Lesson> lessons = lessonRepository.findByInstructorId(instructorId);

        return lessons.stream()
                .map(lesson -> schedulingMapper.toResponse(lesson, instructorName))
                .collect(Collectors.toList());
    }

    public LessonResponse getLessonById(Long id) {
        log.info("Fetching lesson with ID: {}", id);
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        Course course = lesson.getCourse();
        if (course == null) {
            throw new BusinessException("Lesson must be associated with a course", "LESSON_WITHOUT_COURSE");
        }

        String instructorName = instructorHelperService.getInstructorName(course.getInstructorId());

        return schedulingMapper.toResponse(lesson, instructorName);
    }

    public LessonResponse updateLesson(Long id, LessonRequest request) {
        log.info("Updating lesson with ID: {}", id);
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        // Validate student exists and is active if changed
        if (!lesson.getStudentId().equals(request.studentId())) {
            studentHelperService.validateStudentForAction(request.studentId());
            log.debug("Student ID {} validated for action", request.studentId());
        }

        // Load course if courseId is provided
        Course course = null;
        Long instructorId;

        if (request.courseId() != null) {
            course = courseRepository.findById(request.courseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course", request.courseId()));
            instructorId = course.getInstructorId();
        } else {
            // For update, course must be provided
            course = lesson.getCourse();
            if (course == null) {
                throw new BusinessException("Course ID is required for updating lesson", "MISSING_COURSE_ID");
            }
            instructorId = course.getInstructorId();
        }

        // Validate instructor exists (this also gets the name)
        String instructorName = instructorHelperService.getInstructorName(instructorId);
        log.debug("Instructor ID {} validated", instructorId);

        // Validate vehicle exists and is available for use
        vehicleHelperService.validateVehicleForUse(course.getVehicleId());
        log.debug("Vehicle ID {} validated for use", course.getVehicleId());

        // Calculate endTime if not provided (default: startTime + 1h30)
        LocalDateTime endTime = request.endTime();
        if (endTime == null) {
            endTime = request.startTime().plus(Duration.ofHours(1).plusMinutes(30));
            log.debug("EndTime not provided, calculated as startTime + 1h30: {}", endTime);
        }

        // Check for conflicts
        List<Lesson> conflicts = lessonRepository.findConflictingLessons(
                instructorId,
                request.startTime(),
                endTime);

        conflicts.removeIf(l -> l.getId().equals(id));
        if (!conflicts.isEmpty()) {
            throw new BusinessException("Instructor is not available at the requested time", "SCHEDULING_CONFLICT");
        }

        schedulingMapper.updateEntity(lesson, request, course, endTime);
        lesson = lessonRepository.save(lesson);

        kafkaTemplate.send("lesson-updated", lesson.getId().toString(), lesson);
        log.info("Lesson updated with ID: {}", lesson.getId());

        return schedulingMapper.toResponse(lesson, instructorName);
    }

    public void cancelLesson(Long id) {
        log.info("Cancelling lesson with ID: {}", id);
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        lesson.setStatus(Lesson.LessonStatus.CANCELLED);
        lessonRepository.save(lesson);

        kafkaTemplate.send("lesson-cancelled", lesson.getId().toString(), lesson);
        log.info("Lesson cancelled with ID: {}", id);
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
        List<Lesson> lessons;

        if (status != null) {
            lessons = lessonRepository.findByStudentIdAndStatus(studentId, status);
        } else {
            lessons = lessonRepository.findByStudentId(studentId);
        }

        return lessons.stream()
                .map(lesson -> {
                    Course course = lesson.getCourse();
                    if (course == null) {
                        throw new BusinessException("Lesson must be associated with a course", "LESSON_WITHOUT_COURSE");
                    }
                    String instructorName = instructorHelperService.getInstructorName(course.getInstructorId());
                    return schedulingMapper.toResponse(lesson, instructorName);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Fetching lessons between {} and {}", startTime, endTime);
        List<Lesson> lessons = lessonRepository.findByDateRange(startTime, endTime);

        return lessons.stream()
                .map(lesson -> {
                    Course course = lesson.getCourse();
                    if (course == null) {
                        throw new BusinessException("Lesson must be associated with a course", "LESSON_WITHOUT_COURSE");
                    }
                    String instructorName = instructorHelperService.getInstructorName(course.getInstructorId());
                    return schedulingMapper.toResponse(lesson, instructorName);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getUpcomingLessonsByStudent(Long studentId) {
        log.info("Fetching upcoming lessons for student ID: {}", studentId);
        List<Lesson> lessons = lessonRepository.findUpcomingByStudentId(studentId, LocalDateTime.now());

        return lessons.stream()
                .map(lesson -> {
                    Course course = lesson.getCourse();
                    if (course == null) {
                        throw new BusinessException("Lesson must be associated with a course", "LESSON_WITHOUT_COURSE");
                    }
                    String instructorName = instructorHelperService.getInstructorName(course.getInstructorId());
                    return schedulingMapper.toResponse(lesson, instructorName);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsByCourse(Long courseId) {
        log.info("Fetching lessons for course ID: {}", courseId);
        List<Lesson> lessons = lessonRepository.findByCourseId(courseId);

        return lessons.stream()
                .map(lesson -> {
                    Course course = lesson.getCourse();
                    if (course == null) {
                        throw new BusinessException("Lesson must be associated with a course", "LESSON_WITHOUT_COURSE");
                    }
                    String instructorName = instructorHelperService.getInstructorName(course.getInstructorId());
                    return schedulingMapper.toResponse(lesson, instructorName);
                })
                .collect(Collectors.toList());
    }
}

