package com.drivingschool.gateway.auth.dto.student;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record RegisterStudentProfilePayload(
        @NotBlank(message = "Student firstName is required")
        String firstName,
        @NotBlank(message = "Student lastName is required")
        String lastName,
        @NotBlank(message = "Student CNP is required")
        String cnp,
        @Email(message = "Student email must be valid")
        @NotBlank(message = "Student email is required")
        String email,
        @Pattern(regexp = "^[0-9]{10}$", message = "Student phone must be 10 digits")
        @NotBlank(message = "Student phone is required")
        String phone,
        @NotBlank(message = "Student address is required")
        String address,
        @Valid
        RegisterStudentEmergencyProfilePayload profile,
        @NotEmpty(message = "At least one target driving category is required")
        List<String> targetDrivingCategoryCodes
) {
}

