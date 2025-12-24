package com.drivingschool.student.dto;

import com.drivingschool.student.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String cnp;
    private String email;
    private String phone;
    private String address;
    private Student.StudentStatus status;
    private LocalDateTime registrationDate;
    private LocalDateTime lastModifiedDate;
    private List<DocumentResponse> documents;
}

