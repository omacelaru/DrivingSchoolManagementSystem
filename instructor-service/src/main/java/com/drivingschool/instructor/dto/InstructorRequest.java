package com.drivingschool.instructor.dto;

import com.drivingschool.instructor.entity.Instructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO for creating an instructor")
public record InstructorRequest(
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "Instructor first name", example = "Ion")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Instructor last name", example = "Popescu")
    String lastName,

    @NotBlank(message = "License number is required")
    @Size(max = 50, message = "License number must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z]{2,10}-[0-9]{1,10}$", message = "License number must be in format: PREFIX-NUMBER (e.g., LIC-12345)")
    @Schema(description = "Instructor license number", example = "LIC-12345")
    String licenseNumber,

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Schema(description = "Instructor email address", example = "ion.popescu@drivingschool.com")
    String email,

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    @NotBlank(message = "Phone is required")
    @Schema(description = "Instructor phone number", example = "0712345678")
    String phone,

    @NotNull(message = "Specialization is required")
    @Schema(description = "Instructor specialization", example = "BOTH")
    Instructor.Specialization specialization
) {
}

