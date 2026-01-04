package com.drivingschool.instructor.dto;

import com.drivingschool.instructor.entity.Instructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response DTO for instructor information")
public record InstructorResponse(
    @Schema(description = "Instructor ID", example = "1")
    Long id,

    @Schema(description = "Instructor first name", example = "Ion")
    String firstName,

    @Schema(description = "Instructor last name", example = "Popescu")
    String lastName,

    @Schema(description = "Instructor license number", example = "LIC-12345")
    String licenseNumber,

    @Schema(description = "Instructor email address", example = "ion.popescu@drivingschool.com")
    String email,

    @Schema(description = "Instructor phone number", example = "0712345678")
    String phone,

    @Schema(description = "Instructor specialization", example = "BOTH")
    Instructor.Specialization specialization,

    @Schema(description = "Instructor rating", example = "4.5")
    Double rating,

    @Schema(description = "Creation date")
    LocalDateTime createdAt,

    @Schema(description = "Last modification date")
    LocalDateTime lastModifiedDate
) {
}

