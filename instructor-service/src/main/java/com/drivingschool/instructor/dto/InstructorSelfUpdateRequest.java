package com.drivingschool.instructor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO for instructor self-profile updates (restricted fields)")
public record InstructorSelfUpdateRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        @Schema(description = "Instructor first name", example = "Ion")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        @Schema(description = "Instructor last name", example = "Popescu")
        String lastName,

        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
        @NotBlank(message = "Phone is required")
        @Schema(description = "Instructor phone number", example = "0712345678")
        String phone
) {
}
