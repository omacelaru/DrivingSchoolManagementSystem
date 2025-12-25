package com.drivingschool.scheduling.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.scheduling.dto.LessonRequest;
import com.drivingschool.scheduling.entity.Instructor;
import com.drivingschool.scheduling.entity.Lesson;
import com.drivingschool.scheduling.mapper.SchedulingMapper;
import com.drivingschool.scheduling.repository.InstructorRepository;
import com.drivingschool.scheduling.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private SchedulingMapper schedulingMapper;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private SchedulingService schedulingService;

    private LessonRequest lessonRequest;
    private Instructor instructor;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        lessonRequest = new LessonRequest();
        lessonRequest.setStudentId(1L);
        lessonRequest.setInstructorId(1L);
        lessonRequest.setStartTime(LocalDateTime.now().plusDays(1));
        lessonRequest.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        lessonRequest.setType(Lesson.LessonType.PRACTICAL);

        instructor = new Instructor();
        instructor.setId(1L);
        instructor.setFirstName("John");
        instructor.setLastName("Instructor");

        lesson = new Lesson();
        lesson.setId(1L);
        lesson.setStudentId(1L);
        lesson.setInstructor(instructor);
        lesson.setStartTime(lessonRequest.getStartTime());
        lesson.setEndTime(lessonRequest.getEndTime());
        lesson.setType(Lesson.LessonType.PRACTICAL);
        lesson.setStatus(Lesson.LessonStatus.SCHEDULED);
    }

    @Test
    void testBookLesson_Success() {
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(lessonRepository.findConflictingLessons(any(), any(), any())).thenReturn(new ArrayList<>());
        when(schedulingMapper.toEntity(any(), any())).thenReturn(lesson);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);

        assertDoesNotThrow(() -> {
            schedulingService.bookLesson(lessonRequest);
        });

        verify(lessonRepository, times(1)).save(any(Lesson.class));
    }

    @Test
    void testBookLesson_InstructorNotFound() {
        when(instructorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            schedulingService.bookLesson(lessonRequest);
        });
    }

    @Test
    void testBookLesson_Conflict() {
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(lessonRepository.findConflictingLessons(any(), any(), any())).thenReturn(java.util.Arrays.asList(lesson));

        assertThrows(BusinessException.class, () -> {
            schedulingService.bookLesson(lessonRequest);
        });
    }
}

