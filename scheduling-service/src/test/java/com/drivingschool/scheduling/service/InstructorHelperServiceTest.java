package com.drivingschool.scheduling.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.scheduling.client.InstructorClient;
import com.drivingschool.scheduling.dto.InstructorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorHelperServiceTest {

    @Mock
    private InstructorClient instructorClient;

    private InstructorHelperService instructorHelperService;

    private InstructorResponse instructorResponse;

    @BeforeEach
    void setUp() {
        // Create service with self-injection pattern for testing
        instructorHelperService = spy(new InstructorHelperService(instructorClient, null));
        // Set self reference using reflection
        try {
            Field selfField = InstructorHelperService.class.getDeclaredField("self");
            selfField.setAccessible(true);
            selfField.set(instructorHelperService, instructorHelperService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set self reference", e);
        }

        instructorResponse = new InstructorResponse(
                1L,
                "John",
                "Smith",
                "LIC-12345",
                "john.smith@example.com",
                "0712345678",
                "BOTH",
                4.5,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void whenGetInstructorOrThrow_thenReturnsInstructorResponse() {
        // Given
        ApiResult<InstructorResponse> apiResult = ApiResult.success(instructorResponse);
        when(instructorClient.getInstructorById(1L)).thenReturn(apiResult);

        // When
        InstructorResponse result = instructorHelperService.getInstructorOrThrow(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("John", result.firstName());
        assertEquals("Smith", result.lastName());
    }

    @Test
    void whenGetInstructorOrThrowWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        when(instructorClient.getInstructorById(1L)).thenReturn(null);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> instructorHelperService.getInstructorOrThrow(1L));
    }

    @Test
    void whenGetInstructorOrThrowWithNullData_thenThrowsResourceNotFoundException() {
        // Given
        ApiResult<InstructorResponse> apiResult = ApiResult.success(null);
        when(instructorClient.getInstructorById(1L)).thenReturn(apiResult);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> instructorHelperService.getInstructorOrThrow(1L));
    }

    @Test
    void whenGetInstructorName_thenReturnsInstructorName() {
        // Given
        doReturn(instructorResponse).when(instructorHelperService).getInstructorOrThrow(1L);

        // When
        String result = instructorHelperService.getInstructorName(1L);

        // Then
        assertNotNull(result);
        assertEquals("John Smith", result);
    }

    @Test
    void whenGetInstructorNameWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        doThrow(new ResourceNotFoundException("Instructor", 1L))
                .when(instructorHelperService).getInstructorOrThrow(1L);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> instructorHelperService.getInstructorName(1L));
    }

    @Test
    void whenGetInstructorOrThrowFallback_thenReturnsFallbackResponse() {
        // Given
        RuntimeException exception = new RuntimeException("Service down");

        // When
        InstructorResponse result = instructorHelperService.getInstructorOrThrowFallback(1L, exception);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Instructor", result.firstName());
        assertEquals("Unavailable (Fallback)", result.lastName());
        assertEquals("FALLBACK-LICENSE", result.licenseNumber());
    }
}

