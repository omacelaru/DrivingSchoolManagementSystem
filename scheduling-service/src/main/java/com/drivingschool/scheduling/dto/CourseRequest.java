package com.drivingschool.scheduling.dto;

import com.drivingschool.scheduling.entity.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request DTO for creating a course")
public record CourseRequest(
    @NotBlank(message = "Course name is required")
    @Size(max = 200, message = "Course name must not exceed 200 characters")
    @Schema(description = "Course name", example = "Beginner Course")
    String name,

    @Size(max = 1000, message = "Course description must not exceed 1000 characters")
    @Schema(description = "Course description", example = "Complete beginner course with 10 practical lessons")
    String description,

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Schema(description = "Course price", example = "1000.00")
    BigDecimal price,

    @NotNull(message = "Instructor ID is required")
    @Positive(message = "Instructor ID must be positive")
    @Schema(description = "ID of the instructor assigned to this course", example = "1")
    Long instructorId,

    @NotNull(message = "Vehicle ID is required")
    @Positive(message = "Vehicle ID must be positive")
    @Schema(description = "ID of the vehicle assigned to this course", example = "1")
    Long vehicleId,

    @NotNull(message = "Number of lessons is required")
    @Positive(message = "Number of lessons must be positive")
    @Min(value = 1, message = "Number of lessons must be at least 1")
    @Max(value = 100, message = "Number of lessons must not exceed 100")
    @Schema(description = "Number of lessons included in this course", example = "10")
    Integer numberOfLessons,

    @NotNull(message = "Course type is required")
    @Schema(description = "Type of course (THEORETICAL or PRACTICAL)", example = "PRACTICAL")
    Course.CourseType courseType
) {
}

