package com.drivingschool.gateway.auth.dto.instructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterInstructorProfilePayload(
        @NotBlank(message = "Instructor firstName is required")
        String firstName,
        @NotBlank(message = "Instructor lastName is required")
        String lastName,
        @NotBlank(message = "Instructor licenseNumber is required")
        @Pattern(regexp = "^[A-Z]{2,10}-[0-9]{1,10}$", message = "License number must be in format PREFIX-NUMBER")
        String licenseNumber,
        @Email(message = "Instructor email must be valid")
        @NotBlank(message = "Instructor email is required")
        String email,
        @Pattern(regexp = "^[0-9]{10}$", message = "Instructor phone must be 10 digits")
        @NotBlank(message = "Instructor phone is required")
        String phone,
        @NotBlank(message = "Instructor specialization is required")
        String specialization
) {
}

