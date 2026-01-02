package com.drivingschool.instructor.dto;

import com.drivingschool.instructor.entity.Instructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Request DTO for creating an instructor")
public class InstructorRequest {
    @NotBlank(message = "First name is required")
    @Schema(description = "Instructor first name", example = "Ion")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Instructor last name", example = "Popescu")
    private String lastName;

    @NotBlank(message = "License number is required")
    @Schema(description = "Instructor license number", example = "LIC-12345")
    private String licenseNumber;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Schema(description = "Instructor email address", example = "ion.popescu@drivingschool.com")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    @Schema(description = "Instructor phone number", example = "0712345678")
    private String phone;

    @NotNull(message = "Specialization is required")
    @Schema(description = "Instructor specialization", example = "BOTH")
    private Instructor.Specialization specialization;
}

