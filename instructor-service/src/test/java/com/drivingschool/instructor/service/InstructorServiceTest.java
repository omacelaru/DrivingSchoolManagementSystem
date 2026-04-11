package com.drivingschool.instructor.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.instructor.client.SchedulingClient;
import com.drivingschool.instructor.dto.InstructorRequest;
import com.drivingschool.instructor.dto.InstructorResponse;
import com.drivingschool.instructor.entity.Instructor;
import com.drivingschool.instructor.fixture.InstructorFixture;
import com.drivingschool.instructor.mapper.InstructorMapper;
import org.mapstruct.factory.Mappers;
import com.drivingschool.instructor.repository.InstructorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock
    private InstructorRepository instructorRepository;

    private final InstructorMapper instructorMapper = Mappers.getMapper(InstructorMapper.class);

    @Mock
    private SchedulingClient schedulingClient;

    private InstructorService instructorService;

    private InstructorRequest instructorRequest;
    private Instructor instructor;

    @BeforeEach
    void setUp() {
        instructorRequest = InstructorFixture.instructorRequest();
        instructor = InstructorFixture.instructor();

        instructorService = new InstructorService(
                instructorRepository,
                instructorMapper,
                schedulingClient
        );
    }

    @Test
    void whenCreateInstructor_thenReturnsInstructorResponse() {
        // Given
        String licenseNumber = InstructorFixture.defaultLicenseNumber();
        String email = InstructorFixture.defaultEmail();
        String firstName = InstructorFixture.defaultFirstName();
        Long instructorId = InstructorFixture.defaultInstructorId();

        when(instructorRepository.findByLicenseNumber(licenseNumber)).thenReturn(Optional.empty());
        when(instructorRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(instructorRepository.save(any(Instructor.class))).thenAnswer(invocation -> {
            Instructor saved = invocation.getArgument(0);
            saved.setId(instructorId);
            return saved;
        });

        // When
        InstructorResponse result = instructorService.createInstructor(instructorRequest);

        // Then
        assertNotNull(result);
        assertEquals(firstName, result.firstName());
        assertEquals(instructorId, result.id());
        verify(instructorRepository, times(1)).save(any(Instructor.class));
    }

    @Test
    void whenCreateInstructorWithDuplicateLicenseNumber_thenThrowsBusinessException() {
        // Given
        String licenseNumber = InstructorFixture.defaultLicenseNumber();

        when(instructorRepository.findByLicenseNumber(licenseNumber)).thenReturn(Optional.of(instructor));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> instructorService.createInstructor(instructorRequest));

        assertEquals(ErrorCode.DUPLICATE_LICENSE_NUMBER.getCode(), exception.getErrorCode());
        verify(instructorRepository, never()).save(any(Instructor.class));
    }

    @Test
    void whenCreateInstructorWithDuplicateEmail_thenThrowsBusinessException() {
        // Given
        String licenseNumber = InstructorFixture.defaultLicenseNumber();
        String email = InstructorFixture.defaultEmail();

        when(instructorRepository.findByLicenseNumber(licenseNumber)).thenReturn(Optional.empty());
        when(instructorRepository.findByEmail(email)).thenReturn(Optional.of(instructor));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> instructorService.createInstructor(instructorRequest));

        assertEquals(ErrorCode.DUPLICATE_EMAIL.getCode(), exception.getErrorCode());
        verify(instructorRepository, never()).save(any(Instructor.class));
    }

    @Test
    void whenGetInstructorById_thenReturnsInstructorResponse() {
        // Given
        Long instructorId = InstructorFixture.defaultInstructorId();
        when(instructorRepository.findById(instructorId)).thenReturn(Optional.of(instructor));

        // When
        InstructorResponse result = instructorService.getInstructorById(instructorId);

        // Then
        assertNotNull(result);
        assertEquals(instructorId, result.id());
    }

    @Test
    void whenGetInstructorByIdWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        Long instructorId = InstructorFixture.defaultInstructorId();
        when(instructorRepository.findById(instructorId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> instructorService.getInstructorById(instructorId));
    }

    @Test
    void whenGetAllInstructors_thenReturnsAllInstructors() {
        // Given
        int expectedInstructorsCount = 1;

        List<Instructor> instructors = Collections.singletonList(instructor);
        when(instructorRepository.findAll()).thenReturn(instructors);

        // When
        List<InstructorResponse> result = instructorService.getAllInstructors();

        // Then
        assertNotNull(result);
        assertEquals(expectedInstructorsCount, result.size());
    }

    @Test
    void whenGetAvailableInstructors_thenReturnsAvailableInstructorsList() {
        // Given
        Long instructorId = InstructorFixture.defaultInstructorId();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        int expectedInstructorsCount = 1;

        List<Instructor> allInstructors = Collections.singletonList(instructor);
        when(instructorRepository.findAll()).thenReturn(allInstructors);
        when(schedulingClient.isInstructorAvailable(instructorId, startTime, endTime)).thenReturn(true);

        // When
        List<InstructorResponse> result = instructorService.getAvailableInstructors(startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(expectedInstructorsCount, result.size());
        assertEquals(instructorId, result.getFirst().id());
    }

    @Test
    void whenGetAvailableInstructorsWithUnavailableInstructors_thenFiltersOutUnavailable() {
        // Given
        Long instructorId = InstructorFixture.defaultInstructorId();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        int expectedInstructorsCount = 0;

        List<Instructor> allInstructors = Collections.singletonList(instructor);
        when(instructorRepository.findAll()).thenReturn(allInstructors);
        when(schedulingClient.isInstructorAvailable(instructorId, startTime, endTime)).thenReturn(false);

        // When
        List<InstructorResponse> result = instructorService.getAvailableInstructors(startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(expectedInstructorsCount, result.size());
    }

    @Test
    void whenGetAvailableInstructorsAndSchedulingClientFails_thenReturnsEmptyList() {
        // Given
        Long instructorId = InstructorFixture.defaultInstructorId();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        String errorMessage = "Service unavailable";
        int expectedInstructorsCount = 0;

        List<Instructor> allInstructors = Collections.singletonList(instructor);
        when(instructorRepository.findAll()).thenReturn(allInstructors);
        when(schedulingClient.isInstructorAvailable(instructorId, startTime, endTime))
                .thenThrow(new RuntimeException(errorMessage));

        // When
        List<InstructorResponse> result = instructorService.getAvailableInstructors(startTime, endTime);

        // Then - Should return empty list (fail-safe)
        assertNotNull(result);
        assertEquals(expectedInstructorsCount, result.size());
    }

    @Test
    void whenGetInstructorsBySpecialization_thenReturnsInstructorsWithSpecialization() {
        // Given
        Instructor.Specialization specialization = Instructor.Specialization.BOTH;
        int expectedInstructorsCount = 1;

        List<Instructor> instructors = Collections.singletonList(instructor);
        when(instructorRepository.findBySpecialization(specialization)).thenReturn(instructors);

        // When
        List<InstructorResponse> result = instructorService.getInstructorsBySpecialization(specialization);

        // Then
        assertNotNull(result);
        assertEquals(expectedInstructorsCount, result.size());
    }

    @Test
    void whenUpdateInstructor_thenReturnsUpdatedResponse() {
        Long instructorId = InstructorFixture.defaultInstructorId();
        when(instructorRepository.findById(instructorId)).thenReturn(Optional.of(instructor));
        when(instructorRepository.save(any(Instructor.class))).thenAnswer(inv -> inv.getArgument(0));

        InstructorResponse result = instructorService.updateInstructor(instructorId, instructorRequest);

        assertNotNull(result);
        assertEquals(instructorId, result.id());
        verify(instructorRepository).save(any(Instructor.class));
    }

    @Test
    void whenUpdateInstructorWithDuplicateEmail_thenThrowsBusinessException() {
        Long instructorId = InstructorFixture.defaultInstructorId();
        InstructorRequest newEmailRequest = new InstructorRequest(
                instructor.getFirstName(),
                instructor.getLastName(),
                instructor.getLicenseNumber(),
                "taken@example.com",
                instructor.getPhone(),
                instructor.getSpecialization()
        );
        Instructor other = Instructor.builder().id(99L).email("taken@example.com").licenseNumber("LIC-99999").build();
        when(instructorRepository.findById(instructorId)).thenReturn(Optional.of(instructor));
        when(instructorRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(other));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> instructorService.updateInstructor(instructorId, newEmailRequest));
        assertEquals(ErrorCode.DUPLICATE_EMAIL.getCode(), ex.getErrorCode());
    }

    @Test
    void whenUpdateInstructorNotFound_thenThrowsResourceNotFoundException() {
        Long instructorId = InstructorFixture.defaultInstructorId();
        when(instructorRepository.findById(instructorId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> instructorService.updateInstructor(instructorId, instructorRequest));
    }

    @Test
    void whenDeleteInstructor_thenDeletes() {
        Long instructorId = InstructorFixture.defaultInstructorId();
        when(instructorRepository.existsById(instructorId)).thenReturn(true);
        when(schedulingClient.fetchInstructorCourseAssignmentExists(instructorId)).thenReturn(ApiResult.success(false));

        instructorService.deleteInstructor(instructorId);

        verify(instructorRepository).deleteById(instructorId);
    }

    @Test
    void whenDeleteInstructorWithAssignedCourses_thenThrowsBusinessException() {
        Long instructorId = InstructorFixture.defaultInstructorId();
        when(instructorRepository.existsById(instructorId)).thenReturn(true);
        when(schedulingClient.fetchInstructorCourseAssignmentExists(instructorId)).thenReturn(ApiResult.success(true));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> instructorService.deleteInstructor(instructorId));
        assertEquals(ErrorCode.INSTRUCTOR_HAS_SCHEDULING.getCode(), ex.getErrorCode());
        verify(instructorRepository, never()).deleteById(any());
    }

    @Test
    void whenDeleteInstructorWhenSchedulingUnavailable_thenThrowsBusinessException() {
        Long instructorId = InstructorFixture.defaultInstructorId();
        when(instructorRepository.existsById(instructorId)).thenReturn(true);
        when(schedulingClient.fetchInstructorCourseAssignmentExists(instructorId))
                .thenThrow(new RuntimeException("connection refused"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> instructorService.deleteInstructor(instructorId));
        assertEquals(ErrorCode.SCHEDULING_DEPENDENCY_CHECK_FAILED.getCode(), ex.getErrorCode());
        verify(instructorRepository, never()).deleteById(any());
    }

    @Test
    void whenDeleteInstructorNotFound_thenThrowsResourceNotFoundException() {
        Long instructorId = InstructorFixture.defaultInstructorId();
        when(instructorRepository.existsById(instructorId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> instructorService.deleteInstructor(instructorId));
        verify(instructorRepository, never()).deleteById(any());
    }

    @Test
    void whenGetInstructorsBySpecializationWithNoMatches_thenReturnsEmptyList() {
        // Given
        Instructor.Specialization specialization = Instructor.Specialization.THEORETICAL;
        int expectedInstructorsCount = 0;

        when(instructorRepository.findBySpecialization(specialization))
                .thenReturn(Collections.emptyList());

        // When
        List<InstructorResponse> result = instructorService.getInstructorsBySpecialization(specialization);

        // Then
        assertNotNull(result);
        assertEquals(expectedInstructorsCount, result.size());
    }
}

