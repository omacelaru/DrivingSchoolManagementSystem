package com.drivingschool.gateway.auth.dto.instructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterInstructorProfileInput(
        @NotBlank(message = "Instructor firstName is required")
        String firstName,
        @NotBlank(message = "Instructor lastName is required")
        String lastName,
        @NotBlank(message = "Instructor licenseNumber is required")
        @Pattern(regexp = "^[A-Z]{2,10}-[0-9]{1,10}$", message = "License number must be in format PREFIX-NUMBER")
        String licenseNumber,
        @Pattern(regexp = "^[0-9]{10}$", message = "Instructor phone must be 10 digits")
        @NotBlank(message = "Instructor phone is required")
        String phone,
        @NotBlank(message = "Instructor specialization is required")
        String specialization
) {
}

