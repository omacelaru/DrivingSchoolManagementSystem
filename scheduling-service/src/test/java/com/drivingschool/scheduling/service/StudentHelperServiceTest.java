package com.drivingschool.scheduling.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.scheduling.client.StudentClient;
import com.drivingschool.scheduling.dto.StudentResponse;
import com.drivingschool.scheduling.fixture.StudentResponseFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentHelperServiceTest {

    @Mock
    private StudentClient studentClient;

    @InjectMocks
    private StudentHelperService studentHelperService;

    private StudentResponse studentResponse;

    @BeforeEach
    void setUp() {
        studentResponse = StudentResponseFixture.studentResponseActive();
    }

    @Test
    void testGetStudentOrThrow_Success() {
        // Given
        Long studentId = StudentResponseFixture.defaultStudentId();
        String firstName = StudentResponseFixture.defaultFirstName();
        String expectedStatus = StudentResponseFixture.defaultStatus();
        
        ApiResult<StudentResponse> apiResult = ApiResult.success(studentResponse);
        when(studentClient.getStudentById(studentId)).thenReturn(apiResult);

        // When
        StudentResponse result = studentHelperService.getStudentOrThrow(studentId);

        // Then
        assertNotNull(result);
        assertEquals(studentId, result.id());
        assertEquals(firstName, result.firstName());
        assertEquals(expectedStatus, result.status());
    }

    @Test
    void testGetStudentOrThrow_NotFound() {
        // Given
        Long studentId = StudentResponseFixture.defaultStudentId();
        when(studentClient.getStudentById(studentId)).thenReturn(null);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> studentHelperService.getStudentOrThrow(studentId));
    }

    @Test
    void testGetStudentOrThrow_NullData() {
        // Given
        Long studentId = StudentResponseFixture.defaultStudentId();
        ApiResult<StudentResponse> apiResult = ApiResult.success(null);
        when(studentClient.getStudentById(studentId)).thenReturn(apiResult);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> studentHelperService.getStudentOrThrow(studentId));
    }

    @Test
    void testValidateStudentForAction_Success() {
        // Given
        Long studentId = StudentResponseFixture.defaultStudentId();
        ApiResult<StudentResponse> apiResult = ApiResult.success(studentResponse);
        when(studentClient.getStudentById(studentId)).thenReturn(apiResult);

        // When
        assertDoesNotThrow(() -> studentHelperService.validateStudentForAction(studentId));

        // Then - No exception should be thrown
    }

    @Test
    void testValidateStudentForAction_StudentNotFound() {
        // Given
        Long studentId = StudentResponseFixture.defaultStudentId();
        when(studentClient.getStudentById(studentId)).thenReturn(null);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> studentHelperService.validateStudentForAction(studentId));
    }

    @Test
    void testValidateStudentForAction_NotActive() {
        // Given
        Long studentId = StudentResponseFixture.defaultStudentId();
        
        StudentResponse pendingStudent = StudentResponseFixture.studentResponsePending();
        ApiResult<StudentResponse> apiResult = ApiResult.success(pendingStudent);
        when(studentClient.getStudentById(studentId)).thenReturn(apiResult);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> studentHelperService.validateStudentForAction(studentId));

        assertEquals(ErrorCode.STUDENT_NOT_ACTIVE.getCode(), exception.getErrorCode());
    }

    @Test
    void testValidateStudentForAction_Suspended() {
        // Given
        Long studentId = StudentResponseFixture.defaultStudentId();
        
        StudentResponse suspendedStudent = StudentResponseFixture.studentResponseSuspended();
        ApiResult<StudentResponse> apiResult = ApiResult.success(suspendedStudent);
        when(studentClient.getStudentById(studentId)).thenReturn(apiResult);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> studentHelperService.validateStudentForAction(studentId));

        assertEquals(ErrorCode.STUDENT_NOT_ACTIVE.getCode(), exception.getErrorCode());
    }

    @Test
    void testValidateStudentForAction_Graduated() {
        // Given
        Long studentId = StudentResponseFixture.defaultStudentId();
        
        StudentResponse graduatedStudent = StudentResponseFixture.studentResponseGraduated();
        ApiResult<StudentResponse> apiResult = ApiResult.success(graduatedStudent);
        when(studentClient.getStudentById(studentId)).thenReturn(apiResult);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> studentHelperService.validateStudentForAction(studentId));

        assertEquals(ErrorCode.STUDENT_NOT_ACTIVE.getCode(), exception.getErrorCode());
    }
}

