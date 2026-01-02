package com.drivingschool.instructor.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.instructor.dto.InstructorRequest;
import com.drivingschool.instructor.dto.InstructorResponse;
import com.drivingschool.instructor.entity.Instructor;
import com.drivingschool.instructor.mapper.InstructorMapper;
import com.drivingschool.instructor.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public InstructorResponse createInstructor(InstructorRequest request) {
        log.info("Creating instructor with license number: {}", request.getLicenseNumber());

        if (instructorRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
            throw new BusinessException("Instructor with license number " + request.getLicenseNumber() + " already exists", "DUPLICATE_LICENSE_NUMBER");
        }

        if (instructorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Instructor with email " + request.getEmail() + " already exists", "DUPLICATE_EMAIL");
        }

        Instructor instructor = instructorMapper.toEntity(request);
        instructor = instructorRepository.save(instructor);
        log.info("Instructor created with ID: {}", instructor.getId());

        return instructorMapper.toResponse(instructor);
    }

    public InstructorResponse getInstructorById(Long id) {
        log.info("Fetching instructor with ID: {}", id);
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

    public List<Instructor> getAvailableInstructors(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Finding available instructors between {} and {}", startTime, endTime);
        // Note: Availability check should be done by scheduling-service based on lesson conflicts
        // This returns all instructors - scheduling-service will filter based on actual lesson conflicts
        return instructorRepository.findAll();
    }

    public List<InstructorResponse> getInstructorsBySpecialization(Instructor.Specialization specialization) {
        log.info("Fetching instructors with specialization: {}", specialization);
        return instructorRepository.findBySpecialization(specialization).stream()
                .map(instructorMapper::toResponse)
                .collect(Collectors.toList());
    }
}

