//package com.drivingschool.student.controller;
//
//import com.drivingschool.student.dto.StudentRequest;
//import com.drivingschool.student.dto.StudentResponse;
//import com.drivingschool.student.entity.Student;
//import com.drivingschool.student.service.StudentService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(StudentController.class)
//class StudentControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private StudentService studentService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void testCreateStudent() throws Exception {
//        StudentRequest request = new StudentRequest();
//        request.setFirstName("John");
//        request.setLastName("Doe");
//        request.setCnp("1234567890123");
//        request.setEmail("john.doe@example.com");
//        request.setPhone("0123456789");
//        request.setAddress("123 Main St");
//
//        StudentResponse response = StudentResponse.builder()
//                .id(1L)
//                .firstName("John")
//                .lastName("Doe")
//                .cnp("1234567890123")
//                .email("john.doe@example.com")
//                .phone("0123456789")
//                .address("123 Main St")
//                .status(Student.StudentStatus.PENDING)
//                .registrationDate(LocalDateTime.now())
//                .build();
//
//        when(studentService.createStudent(any(StudentRequest.class))).thenReturn(response);
//
//        mockMvc.perform(post("/api/students")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.id").value(1L))
//                .andExpect(jsonPath("$.data.firstName").value("John"));
//    }
//
//    @Test
//    void testGetStudentById() throws Exception {
//        StudentResponse response = StudentResponse.builder()
//                .id(1L)
//                .firstName("John")
//                .lastName("Doe")
//                .cnp("1234567890123")
//                .email("john.doe@example.com")
//                .phone("0123456789")
//                .address("123 Main St")
//                .status(Student.StudentStatus.ACTIVE)
//                .build();
//
//        when(studentService.getStudentById(1L)).thenReturn(response);
//
//        mockMvc.perform(get("/api/students/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.id").value(1L))
//                .andExpect(jsonPath("$.data.firstName").value("John"));
//    }
//
//    @Test
//    void testGetAllStudents() throws Exception {
//        List<StudentResponse> students = Collections.singletonList(
//                StudentResponse.builder().id(1L).firstName("John").lastName("Doe").build(),
//                StudentResponse.builder().id(2L).firstName("Jane").lastName("Smith").build()
//        );
//
//        when(studentService.getAllStudents(null)).thenReturn(students);
//
//        mockMvc.perform(get("/api/students"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.length()").value(2));
//    }
//
//    @Test
//    void testUpdateStudent() throws Exception {
//        StudentRequest request = new StudentRequest();
//        request.setFirstName("John Updated");
//        request.setLastName("Doe");
//        request.setCnp("1234567890123");
//        request.setEmail("john.doe@example.com");
//        request.setPhone("0123456789");
//        request.setAddress("123 Main St");
//
//        StudentResponse response = StudentResponse.builder()
//                .id(1L)
//                .firstName("John Updated")
//                .lastName("Doe")
//                .cnp("1234567890123")
//                .email("john.doe@example.com")
//                .phone("0123456789")
//                .address("123 Main St")
//                .status(Student.StudentStatus.ACTIVE)
//                .build();
//
//        when(studentService.updateStudent(eq(1L), any(StudentRequest.class))).thenReturn(response);
//
//        mockMvc.perform(put("/api/students/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.firstName").value("John Updated"));
//    }
//
//    @Test
//    void testDeleteStudent() throws Exception {
//        mockMvc.perform(delete("/api/students/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true));
//    }
//}
//
