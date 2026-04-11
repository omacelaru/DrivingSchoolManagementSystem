package com.drivingschool.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Optional extended profile / emergency contact for a student")
public record StudentProfileRequest(
        @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
        @Schema(description = "Emergency contact full name", example = "Jane Doe")
        String emergencyContactName,

        @Pattern(regexp = "^$|^[0-9]{10}$", message = "Emergency contact phone must be 10 digits")
        @Schema(description = "Emergency contact phone (10 digits)", example = "0721234567")
        String emergencyContactPhone,

        @Size(max = 2000, message = "Notes must not exceed 2000 characters")
        @Schema(description = "Internal notes (allergies, scheduling preferences, etc.)")
        String notes
) {
}
