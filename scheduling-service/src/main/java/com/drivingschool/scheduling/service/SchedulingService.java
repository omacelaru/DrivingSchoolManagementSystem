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
    private final SchedulingMapper schedulingMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentClient paymentClient;
    
    @Value("${lesson.standard-price}")
    private BigDecimal standardLessonPrice;

    //todo prea mare metoda
    public LessonResponse bookLesson(LessonRequest request) {
        log.info("Booking lesson for student ID: {}", request.getStudentId());

        String instructorName = instructorHelperService.getInstructorName(request.getInstructorId());

        // Check for conflicts
        List<Lesson> conflicts = lessonRepository.findConflictingLessons(
                request.getInstructorId(), 
                request.getStartTime(), 
                request.getEndTime());
        
        if (!conflicts.isEmpty()) {
            throw new BusinessException("Instructor is not available at the requested time", "SCHEDULING_CONFLICT");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot book lessons in the past", "INVALID_TIME");
        }

        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().isEqual(request.getStartTime())) {
            throw new BusinessException("End time must be after start time", "INVALID_TIME_RANGE");
        }

        // Load course if courseId is provided
        Course course = null;
        if (request.getCourseId() != null) {
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));
        }

        Lesson lesson = schedulingMapper.toEntity(request, course);
        lesson = lessonRepository.save(lesson);
        
        // If lesson is not part of a course (additional lesson), create pending payment
        if (request.getCourseId() == null) {
            log.info("Lesson is additional, creating pending payment for lesson ID: {}", lesson.getId());
            try {
                PaymentRequest paymentRequest = new PaymentRequest();
                paymentRequest.setStudentId(request.getStudentId());
                paymentRequest.setAmount(standardLessonPrice);
                paymentRequest.setLessonId(lesson.getId());
                paymentRequest.setNotes("Payment for additional lesson");
                
                ApiResult<PaymentResponse> paymentResult = paymentClient.createPendingPayment(paymentRequest);
                if (paymentResult.isSuccess() && paymentResult.getData() != null) {
                    lesson.setPaymentId(paymentResult.getData().getId());
                    lesson = lessonRepository.save(lesson);
                    log.info("Pending payment created with ID: {} for lesson ID: {}", 
                            paymentResult.getData().getId(), lesson.getId());
                }
            } catch (Exception e) {
                log.error("Failed to create pending payment for lesson ID: {}", lesson.getId(), e);
                // Don't fail the lesson booking if payment creation fails
                // The payment can be created later
            }
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
        
        String instructorName = instructorHelperService.getInstructorName(lesson.getInstructorId());
        
        return schedulingMapper.toResponse(lesson, instructorName);
    }

    public LessonResponse updateLesson(Long id, LessonRequest request) {
        log.info("Updating lesson with ID: {}", id);
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        String instructorName = instructorHelperService.getInstructorName(request.getInstructorId());

        // Check for conflicts
        List<Lesson> conflicts = lessonRepository.findConflictingLessons(
                request.getInstructorId(), 
                request.getStartTime(), 
                request.getEndTime());
        
        conflicts.removeIf(l -> l.getId().equals(id));
        if (!conflicts.isEmpty()) {
            throw new BusinessException("Instructor is not available at the requested time", "SCHEDULING_CONFLICT");
        }

        // Load course if courseId is provided
        Course course = null;
        if (request.getCourseId() != null) {
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));
        }

        schedulingMapper.updateEntity(lesson, request, course);
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
        List<Lesson> conflicts = lessonRepository.findConflictingLessons(instructorId, startTime, endTime);
        return conflicts.isEmpty();
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
                    String instructorName = instructorHelperService.getInstructorName(lesson.getInstructorId());
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
                    String instructorName = instructorHelperService.getInstructorName(lesson.getInstructorId());
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
                    String instructorName = instructorHelperService.getInstructorName(lesson.getInstructorId());
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
                    String instructorName = instructorHelperService.getInstructorName(lesson.getInstructorId());
                    return schedulingMapper.toResponse(lesson, instructorName);
                })
                .collect(Collectors.toList());
    }
}

