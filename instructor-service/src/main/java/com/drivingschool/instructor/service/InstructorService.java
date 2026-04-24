package com.drivingschool.instructor.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.dto.PageResponse;
import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.common.mapper.PageResponseMapper;
import com.drivingschool.common.pagination.PageableFactory;
import com.drivingschool.instructor.client.SchedulingClient;
import com.drivingschool.instructor.dto.InstructorRequest;
import com.drivingschool.instructor.dto.InstructorResponse;
import com.drivingschool.instructor.entity.Instructor;
import com.drivingschool.instructor.mapper.InstructorMapper;
import com.drivingschool.instructor.pagination.InstructorSortField;
import com.drivingschool.instructor.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Value("${app.pagination.default-page-size:20}")
    private int defaultPageSize;

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
            throw new BusinessException("Instructor with license number " + licenseNumber + " already exists", ErrorCode.DUPLICATE_LICENSE_NUMBER);
        }

        if (instructorRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("Instructor with email " + email + " already exists", ErrorCode.DUPLICATE_EMAIL);
        }
    }

    @Cacheable(value = "instructors", key = "#id")
    @Transactional(readOnly = true)
    public InstructorResponse getInstructorById(Long id) {
        log.info("Fetching instructor with ID: {} from database", id);
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor", id));
        return instructorMapper.toResponse(instructor);
    }

    @CacheEvict(value = "instructors", allEntries = true)
    public InstructorResponse updateInstructor(Long id, InstructorRequest request) {
        log.info("Updating instructor with ID: {}", id);
        Instructor instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor", id));
        validateInstructorUniquenessForUpdate(instructor, request);
        instructorMapper.updateEntity(instructor, request);
        Instructor updated = instructorRepository.save(instructor);
        log.info("Instructor updated with ID: {}", updated.getId());
        return instructorMapper.toResponse(updated);
    }

    private void validateInstructorUniquenessForUpdate(Instructor existing, InstructorRequest request) {
        if (!existing.getLicenseNumber().equals(request.licenseNumber())
                && instructorRepository.findByLicenseNumber(request.licenseNumber()).isPresent()) {
            throw new BusinessException(
                    "Instructor with license number " + request.licenseNumber() + " already exists",
                    ErrorCode.DUPLICATE_LICENSE_NUMBER);
        }
        if (!existing.getEmail().equals(request.email())
                && instructorRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException(
                    "Instructor with email " + request.email() + " already exists",
                    ErrorCode.DUPLICATE_EMAIL);
        }
    }

    @CacheEvict(value = "instructors", allEntries = true)
    public void deleteInstructor(Long id) {
        log.info("Deleting instructor with ID: {}", id);
        if (!instructorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Instructor", id);
        }
        boolean hasCourses;
        try {
            ApiResult<Boolean> res = schedulingClient.fetchInstructorCourseAssignmentExists(id);
            hasCourses = res != null && Boolean.TRUE.equals(res.data());
        } catch (Exception e) {
            log.error("Scheduling service unreachable while checking instructor {}: {}", id, e.getMessage());
            throw new BusinessException(
                    "Cannot verify scheduling data; try again later or ensure scheduling-service is available.",
                    ErrorCode.SCHEDULING_DEPENDENCY_CHECK_FAILED);
        }
        if (hasCourses) {
            throw new BusinessException(
                    "Cannot delete instructor with assigned courses. Remove or reassign courses first.",
                    ErrorCode.INSTRUCTOR_HAS_SCHEDULING);
        }
        instructorRepository.deleteById(id);
        log.info("Instructor deleted with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public PageResponse<InstructorResponse> getInstructorsPage(
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable = PageableFactory.build(
                page, size, sortBy, sortDir, defaultPageSize, InstructorSortField.class
        );
        Page<Instructor> instructorPage = instructorRepository.findAll(pageable);
        return PageResponseMapper.from(instructorPage.map(instructorMapper::toResponse));
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

