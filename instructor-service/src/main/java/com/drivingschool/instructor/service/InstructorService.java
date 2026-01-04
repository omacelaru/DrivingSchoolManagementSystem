package com.drivingschool.instructor.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.instructor.client.SchedulingClient;
import com.drivingschool.instructor.dto.InstructorRequest;
import com.drivingschool.instructor.dto.InstructorResponse;
import com.drivingschool.instructor.entity.Instructor;
import com.drivingschool.instructor.mapper.InstructorMapper;
import com.drivingschool.instructor.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InstructorService {
    private final InstructorRepository instructorRepository;
    private final InstructorMapper instructorMapper;
    private final SchedulingClient schedulingClient;

    @CacheEvict(value = "instructors", allEntries = true)
    public InstructorResponse createInstructor(InstructorRequest request) {
        log.info("Creating instructor with license number: {}", request.licenseNumber());

        validateInstructorUniqueness(request.licenseNumber(), request.email());
        Instructor instructor = instructorMapper.toEntity(request);
        instructor = instructorRepository.save(instructor);
        log.info("Instructor created with ID: {}", instructor.getId());

        return instructorMapper.toResponse(instructor);
    }

    private void validateInstructorUniqueness(String licenseNumber, String email) {
        if (instructorRepository.findByLicenseNumber(licenseNumber).isPresent()) {
            throw new BusinessException("Instructor with license number " + licenseNumber + " already exists", "DUPLICATE_LICENSE_NUMBER");
        }

        if (instructorRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("Instructor with email " + email + " already exists", "DUPLICATE_EMAIL");
        }
    }

    @Cacheable(value = "instructors", key = "#id")
    @Transactional(readOnly = true)
    public InstructorResponse getInstructorById(Long id) {
        log.info("Fetching instructor with ID: {} from database", id);
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new com.drivingschool.common.exception.ResourceNotFoundException("Instructor", id));
        return instructorMapper.toResponse(instructor);
    }

    public List<InstructorResponse> getAllInstructors() {
        log.info("Fetching all instructors");
        return instructorRepository.findAll().stream()
                .map(instructorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InstructorResponse> getAvailableInstructors(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Finding available instructors between {} and {}", startTime, endTime);
        
        List<Instructor> allInstructors = instructorRepository.findAll();
        
        return allInstructors.stream()
                .filter(instructor -> isInstructorAvailableForScheduling(instructor.getId(), startTime, endTime))
                .map(instructorMapper::toResponse)
                .collect(Collectors.toList());
    }

    private boolean isInstructorAvailableForScheduling(Long instructorId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Boolean isAvailable = schedulingClient.isInstructorAvailable(instructorId, startTime, endTime);
            return Boolean.TRUE.equals(isAvailable);
        } catch (Exception e) {
            log.warn("Failed to check availability for instructor {}: {}", instructorId, e.getMessage());
            return false;
        }
    }

    public List<InstructorResponse> getInstructorsBySpecialization(Instructor.Specialization specialization) {
        log.info("Fetching instructors with specialization: {}", specialization);
        return instructorRepository.findBySpecialization(specialization).stream()
                .map(instructorMapper::toResponse)
                .collect(Collectors.toList());
    }
}

