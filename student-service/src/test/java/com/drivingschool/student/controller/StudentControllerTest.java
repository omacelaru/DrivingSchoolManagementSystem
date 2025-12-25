package com.drivingschool.student.controller;

import com.drivingschool.student.dto.StudentRequest;
import com.drivingschool.student.dto.StudentResponse;
import com.drivingschool.student.entity.Student;
import com.drivingschool.student.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(studentController).build();
    }

    @Test
    void testCreateStudent() throws Exception {
        StudentRequest request = new StudentRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setCnp("1234567890123");
        request.setEmail("john.doe@example.com");
        request.setPhone("0123456789");
        request.setAddress("123 Main St");

        StudentResponse response = new StudentResponse();
        response.setId(1L);
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setCnp("1234567890123");
        response.setEmail("john.doe@example.com");
        response.setPhone("0123456789");
        response.setAddress("123 Main St");
        response.setStatus(Student.StudentStatus.PENDING);
        response.setRegistrationDate(LocalDateTime.now());

        when(studentService.createStudent(any(StudentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    void testGetStudentById() throws Exception {
        StudentResponse response = new StudentResponse();
        response.setId(1L);
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setCnp("1234567890123");
        response.setEmail("john.doe@example.com");
        response.setPhone("0123456789");
        response.setAddress("123 Main St");
        response.setStatus(Student.StudentStatus.ACTIVE);

        when(studentService.getStudentById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    void testGetAllStudents() throws Exception {
        StudentResponse student1 = new StudentResponse();
        student1.setId(1L);
        student1.setFirstName("John");
        student1.setLastName("Doe");
        
        StudentResponse student2 = new StudentResponse();
        student2.setId(2L);
        student2.setFirstName("Jane");
        student2.setLastName("Smith");
        
        List<StudentResponse> students = Arrays.asList(student1, student2);

        when(studentService.getAllStudents(null)).thenReturn(students);

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testUpdateStudent() throws Exception {
        StudentRequest request = new StudentRequest();
        request.setFirstName("John Updated");
        request.setLastName("Doe");
        request.setCnp("1234567890123");
        request.setEmail("john.doe@example.com");
        request.setPhone("0123456789");
        request.setAddress("123 Main St");

        StudentResponse response = new StudentResponse();
        response.setId(1L);
        response.setFirstName("John Updated");
        response.setLastName("Doe");
        response.setCnp("1234567890123");
        response.setEmail("john.doe@example.com");
        response.setPhone("0123456789");
        response.setAddress("123 Main St");
        response.setStatus(Student.StudentStatus.ACTIVE);

        when(studentService.updateStudent(eq(1L), any(StudentRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("John Updated"));
    }

    @Test
    void testDeleteStudent() throws Exception {
        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

