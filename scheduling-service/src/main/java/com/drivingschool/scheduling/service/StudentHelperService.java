package com.drivingschool.scheduling.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.scheduling.client.StudentClient;
import com.drivingschool.scheduling.dto.StudentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Helper service for student operations with Redis caching and business validations.
 */
@Service
@Slf4j
public class StudentHelperService {
    private final StudentClient studentClient;

    public StudentHelperService(StudentClient studentClient) {
        this.studentClient = studentClient;
    }

    /**
     * Gets student data from student-service with Redis caching (10 min TTL).
     * 
     * @param studentId student ID
     * @return student data
     * @throws ResourceNotFoundException if student not found
     */
    @Cacheable(value = "studentCache", key = "#studentId")
    public StudentResponse getStudentOrThrow(Long studentId) {
        log.debug("Fetching student with ID: {} from student-service", studentId);
        ApiResult<StudentResponse> studentResult = studentClient.getStudentById(studentId);
        
        if (studentResult == null || studentResult.data() == null) {
            log.warn("Student with ID {} not found", studentId);
            throw new ResourceNotFoundException("Student", studentId);
        }
        
        return studentResult.data();
    }

    /**
     * Validates that a student can perform actions (book lessons, enroll in courses).
     * Checks:
     * - Student exists
     * - Student status is ACTIVE
     * 
     * @param studentId student ID
     * @throws ResourceNotFoundException if student not found
     * @throws BusinessException if student is not active
     */
    public void validateStudentForAction(Long studentId) {
        StudentResponse student = getStudentOrThrow(studentId);
        
        // Check student status
        if (!"ACTIVE".equalsIgnoreCase(student.status())) {
            throw new BusinessException(
                    String.format("Student with ID %d cannot perform this action. Current status: %s. Only ACTIVE students can book lessons or enroll in courses.", 
                            studentId, student.status()),
                    "STUDENT_NOT_ACTIVE");
        }
        
        log.debug("Student ID {} validated for action - status: {}", studentId, student.status());
    }
}

