package com.drivingschool.gateway.auth.dto.student;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterStudentEmergencyProfilePayload(
        @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
        String emergencyContactName,
        @Pattern(regexp = "^$|^[0-9]{10}$", message = "Emergency contact phone must be 10 digits")
        String emergencyContactPhone,
        @Size(max = 2000, message = "Notes must not exceed 2000 characters")
        String notes
) {
}

