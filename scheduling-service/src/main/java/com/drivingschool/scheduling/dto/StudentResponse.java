package com.drivingschool.scheduling.dto;

import lombok.Data;

@Data
public class StudentResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String status; // StudentStatus enum as string
}

