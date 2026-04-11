package com.drivingschool.student.dto;

import com.drivingschool.common.validation.CNP;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Optional;

@Schema(description = "Request DTO for creating or updating a student")
public record StudentRequest(
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "Student's first name", example = "John")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Student's last name", example = "Doe")
    String lastName,

    @CNP
    @NotBlank(message = "CNP is required")
    @Schema(description = "Romanian CNP (Personal Numeric Code)", example = "1234567890123")
    String cnp,

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Schema(description = "Student's email address", example = "john.doe@example.com")
    String email,

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    @NotBlank(message = "Phone is required")
    @Schema(description = "Student's phone number (10 digits)", example = "0123456789")
    String phone,

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Schema(description = "Student's address", example = "123 Main Street, Bucharest")
    String address,

    @Valid
    @Schema(description = "Optional extended profile (emergency contact, notes). Omit or use null in JSON for none.")
    Optional<StudentProfileRequest> profile,

    @NotEmpty(message = "At least one target driving licence category is required")
    @Schema(
            description = "Target driving licence category codes (enum names: AM, A1, B, BE, C, …). Case-insensitive. Required; minimum one code.",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "[\"B\"]"
    )
    List<String> targetDrivingCategoryCodes
) {
    public StudentRequest {
        if (targetDrivingCategoryCodes != null) {
            targetDrivingCategoryCodes = List.copyOf(targetDrivingCategoryCodes);
        }
    }
}

