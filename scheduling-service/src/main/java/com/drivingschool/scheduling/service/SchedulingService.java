package com.drivingschool.scheduling.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.scheduling.dto.LessonRequest;
import com.drivingschool.scheduling.dto.LessonResponse;
import com.drivingschool.scheduling.entity.Instructor;
import com.drivingschool.scheduling.entity.Lesson;
import com.drivingschool.scheduling.mapper.SchedulingMapper;
import com.drivingschool.scheduling.repository.InstructorRepository;
import com.drivingschool.scheduling.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SchedulingService {
    private static final Logger log = LoggerFactory.getLogger(SchedulingService.class);
    private final LessonRepository lessonRepository;
    private final InstructorRepository instructorRepository;
    private final SchedulingMapper schedulingMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SchedulingService(LessonRepository lessonRepository, InstructorRepository instructorRepository, SchedulingMapper schedulingMapper, KafkaTemplate<String, Object> kafkaTemplate) {
        this.lessonRepository = lessonRepository;
        this.instructorRepository = instructorRepository;
        this.schedulingMapper = schedulingMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public LessonResponse bookLesson(LessonRequest request) {
        log.info("Booking lesson for student ID: {}", request.getStudentId());
        
        Instructor instructor = instructorRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor", request.getInstructorId()));

        // Check for conflicts
        List<Lesson> conflicts = lessonRepository.findConflictingLessons(
                request.getInstructorId(), 
                request.getStartTime(), 
                request.getEndTime());
        
        if (!conflicts.isEmpty()) {
            throw new BusinessException("Instructor is not available at the requested time", "SCHEDULING_CONFLICT");
        }

        // Validate time
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot book lessons in the past", "INVALID_TIME");
        }

        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().isEqual(request.getStartTime())) {
            throw new BusinessException("End time must be after start time", "INVALID_TIME_RANGE");
        }

        Lesson lesson = schedulingMapper.toEntity(request, instructor);
        lesson = lessonRepository.save(lesson);
        
        // Publish event to Kafka
        kafkaTemplate.send("lesson-booked", lesson.getId().toString(), lesson);
        log.info("Lesson booked with ID: {}", lesson.getId());
        
        return schedulingMapper.toResponse(lesson);
    }

    public List<Instructor> getAvailableInstructors(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Finding available instructors between {} and {}", startTime, endTime);
        return instructorRepository.findAvailableInstructors(startTime, endTime);
    }

    public List<LessonResponse> getInstructorLessons(Long instructorId) {
        log.info("Fetching lessons for instructor ID: {}", instructorId);
        if (!instructorRepository.existsById(instructorId)) {
            throw new ResourceNotFoundException("Instructor", instructorId);
        }
        
        List<Lesson> lessons = lessonRepository.findByInstructorId(instructorId);
        return lessons.stream()
                .map(schedulingMapper::toResponse)
                .collect(Collectors.toList());
    }

    public LessonResponse getLessonById(Long id) {
        log.info("Fetching lesson with ID: {}", id);
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));
        return schedulingMapper.toResponse(lesson);
    }

    public LessonResponse updateLesson(Long id, LessonRequest request) {
        log.info("Updating lesson with ID: {}", id);
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        Instructor instructor = instructorRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor", request.getInstructorId()));

        // Check for conflicts (excluding current lesson)
        List<Lesson> conflicts = lessonRepository.findConflictingLessons(
                request.getInstructorId(), 
                request.getStartTime(), 
                request.getEndTime());
        
        conflicts.removeIf(l -> l.getId().equals(id));
        if (!conflicts.isEmpty()) {
            throw new BusinessException("Instructor is not available at the requested time", "SCHEDULING_CONFLICT");
        }

        schedulingMapper.updateEntity(lesson, request, instructor);
        lesson = lessonRepository.save(lesson);
        
        kafkaTemplate.send("lesson-updated", lesson.getId().toString(), lesson);
        log.info("Lesson updated with ID: {}", lesson.getId());
        
        return schedulingMapper.toResponse(lesson);
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
}

