package com.drivingschool.scheduling.dto;

import com.drivingschool.scheduling.entity.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request DTO for creating a course")
public class CourseRequest {
    @NotBlank(message = "Course name is required")
    @Schema(description = "Course name", example = "Beginner Course")
    private String name;

    @Schema(description = "Course description", example = "Complete beginner course with 10 practical lessons")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Schema(description = "Course price", example = "1000.00")
    private BigDecimal price;

    @NotNull(message = "Instructor ID is required")
    @Schema(description = "ID of the instructor assigned to this course", example = "1")
    private Long instructorId;

    @NotNull(message = "Vehicle ID is required")
    @Schema(description = "ID of the vehicle assigned to this course", example = "1")
    private Long vehicleId;

    @NotNull(message = "Number of lessons is required")
    @Positive(message = "Number of lessons must be positive")
    @Schema(description = "Number of lessons included in this course", example = "10")
    private Integer numberOfLessons;

    @NotNull(message = "Course type is required")
    @Schema(description = "Type of course (THEORETICAL or PRACTICAL)", example = "PRACTICAL")
    private Course.CourseType courseType;
}

