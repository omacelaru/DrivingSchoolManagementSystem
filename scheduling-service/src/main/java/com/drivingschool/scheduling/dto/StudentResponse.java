package com.drivingschool.scheduling.dto;

public record StudentResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    String status // StudentStatus enum as string
) {
}

