package com.drivingschool.instructor.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.instructor.client.SchedulingClient;
import com.drivingschool.instructor.dto.InstructorRequest;
import com.drivingschool.instructor.dto.InstructorResponse;
import com.drivingschool.instructor.entity.Instructor;
import com.drivingschool.instructor.fixture.InstructorFixture;
import com.drivingschool.instructor.mapper.InstructorMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock
    private InstructorRepository instructorRepository;

    private final InstructorMapper instructorMapper = new InstructorMapper();

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
    void testCreateInstructor_Success() {
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
    void testCreateInstructor_DuplicateLicenseNumber() {
        // Given
        String licenseNumber = InstructorFixture.defaultLicenseNumber();
        String expectedErrorCode = "DUPLICATE_LICENSE_NUMBER";
        
        when(instructorRepository.findByLicenseNumber(licenseNumber)).thenReturn(Optional.of(instructor));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            instructorService.createInstructor(instructorRequest);
        });

        assertEquals(expectedErrorCode, exception.getErrorCode());
        verify(instructorRepository, never()).save(any(Instructor.class));
    }

    @Test
    void testCreateInstructor_DuplicateEmail() {
        // Given
        String licenseNumber = InstructorFixture.defaultLicenseNumber();
        String email = InstructorFixture.defaultEmail();
        String expectedErrorCode = "DUPLICATE_EMAIL";
        
        when(instructorRepository.findByLicenseNumber(licenseNumber)).thenReturn(Optional.empty());
        when(instructorRepository.findByEmail(email)).thenReturn(Optional.of(instructor));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            instructorService.createInstructor(instructorRequest);
        });

        assertEquals(expectedErrorCode, exception.getErrorCode());
        verify(instructorRepository, never()).save(any(Instructor.class));
    }

    @Test
    void testGetInstructorById_Success() {
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
    void testGetInstructorById_NotFound() {
        // Given
        Long instructorId = InstructorFixture.defaultInstructorId();
        when(instructorRepository.findById(instructorId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            instructorService.getInstructorById(instructorId);
        });
    }

    @Test
    void testGetAllInstructors() {
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
    void testGetAvailableInstructors_Success() {
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
    void testGetAvailableInstructors_FiltersOutUnavailable() {
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
    void testGetAvailableInstructors_HandlesSchedulingClientError() {
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
    void testGetInstructorsBySpecialization() {
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
    void testGetInstructorsBySpecialization_EmptyResult() {
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

