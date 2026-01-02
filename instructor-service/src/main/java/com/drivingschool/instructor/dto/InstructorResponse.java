package com.drivingschool.instructor.dto;

import com.drivingschool.instructor.entity.Instructor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for instructor information")
public class InstructorResponse {
    @Schema(description = "Instructor ID", example = "1")
    private Long id;

    @Schema(description = "Instructor first name", example = "Ion")
    private String firstName;

    @Schema(description = "Instructor last name", example = "Popescu")
    private String lastName;

    @Schema(description = "Instructor license number", example = "LIC-12345")
    private String licenseNumber;

    @Schema(description = "Instructor email address", example = "ion.popescu@drivingschool.com")
    private String email;

    @Schema(description = "Instructor phone number", example = "0712345678")
    private String phone;

    @Schema(description = "Instructor specialization", example = "BOTH")
    private Instructor.Specialization specialization;

    @Schema(description = "Instructor rating", example = "4.5")
    private Double rating;

    @Schema(description = "Creation date")
    private LocalDateTime createdAt;

    @Schema(description = "Last modification date")
    private LocalDateTime lastModifiedDate;
}

