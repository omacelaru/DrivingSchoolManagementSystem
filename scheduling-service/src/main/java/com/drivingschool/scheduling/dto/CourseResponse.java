package com.drivingschool.scheduling.dto;

import com.drivingschool.scheduling.entity.Course;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Response DTO containing course information")
public record CourseResponse(
    @Schema(description = "Unique course identifier", example = "1")
    Long id,

    @Schema(description = "Course name", example = "Beginner Course")
    String name,

    @Schema(description = "Course description", example = "Complete beginner course with 10 practical lessons")
    String description,

    @Schema(description = "Course price", example = "1000.00")
    BigDecimal price,

    @Schema(description = "Number of lessons included in the course (configured)", example = "10")
    Integer numberOfLessons,

    @Schema(description = "Type of course (THEORETICAL or PRACTICAL)", example = "PRACTICAL")
    Course.CourseType courseType,

    @Schema(description = "ID of the instructor assigned to this course", example = "1")
    Long instructorId,

    @Schema(description = "ID of the vehicle assigned to this course", example = "1")
    Long vehicleId,

    @Schema(description = "Date and time when course was created", example = "2027-01-01T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "Date and time when course was last modified", example = "2027-01-01T10:30:00")
    LocalDateTime lastModifiedDate,

    @Schema(description = "Course tag codes (e.g. INTENSIVE, WEEKEND)")
    List<String> courseTagCodes
) {
}

