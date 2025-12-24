package com.drivingschool.student.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.student.dto.StudentRequest;
import com.drivingschool.student.dto.StudentResponse;
import com.drivingschool.student.entity.Student;
import com.drivingschool.student.mapper.StudentMapper;
import com.drivingschool.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentService studentService;

    private StudentRequest studentRequest;
    private Student student;
    private StudentResponse studentResponse;

    @BeforeEach
    void setUp() {
        studentRequest = new StudentRequest();
        studentRequest.setFirstName("John");
        studentRequest.setLastName("Doe");
        studentRequest.setCnp("1234567890123");
        studentRequest.setEmail("john.doe@example.com");
        studentRequest.setPhone("0123456789");
        studentRequest.setAddress("123 Main St");

        student = Student.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .cnp("1234567890123")
                .email("john.doe@example.com")
                .phone("0123456789")
                .address("123 Main St")
                .status(Student.StudentStatus.PENDING)
                .build();

        studentResponse = StudentResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .cnp("1234567890123")
                .email("john.doe@example.com")
                .phone("0123456789")
                .address("123 Main St")
                .status(Student.StudentStatus.PENDING)
                .build();
    }

    @Test
    void testCreateStudent_Success() {
        when(studentRepository.existsByCnp(anyString())).thenReturn(false);
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(studentMapper.toEntity(any(StudentRequest.class))).thenReturn(student);
        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(studentMapper.toResponse(any(Student.class))).thenReturn(studentResponse);

        StudentResponse result = studentService.createStudent(studentRequest);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void testCreateStudent_DuplicateCNP() {
        when(studentRepository.existsByCnp(anyString())).thenReturn(true);

        assertThrows(BusinessException.class, () -> {
            studentService.createStudent(studentRequest);
        });
    }

    @Test
    void testGetStudentById_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentMapper.toResponse(any(Student.class))).thenReturn(studentResponse);

        StudentResponse result = studentService.getStudentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetStudentById_NotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            studentService.getStudentById(1L);
        });
    }

    @Test
    void testUpdateStudent_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByCnp(anyString())).thenReturn(false);
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(studentMapper.toResponse(any(Student.class))).thenReturn(studentResponse);

        StudentResponse result = studentService.updateStudent(1L, studentRequest);

        assertNotNull(result);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void testDeleteStudent_Success() {
        when(studentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(studentRepository).deleteById(1L);

        assertDoesNotThrow(() -> {
            studentService.deleteStudent(1L);
        });

        verify(studentRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteStudent_NotFound() {
        when(studentRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            studentService.deleteStudent(1L);
        });
    }
}

