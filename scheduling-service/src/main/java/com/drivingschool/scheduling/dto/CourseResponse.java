package com.drivingschool.scheduling.dto;

import com.drivingschool.scheduling.entity.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing course information")
public class CourseResponse {
    @Schema(description = "Unique course identifier", example = "1")
    private Long id;

    @Schema(description = "Course name", example = "Beginner Course")
    private String name;

    @Schema(description = "Course description", example = "Complete beginner course with 10 practical lessons")
    private String description;

    @Schema(description = "Course price", example = "1000.00")
    private BigDecimal price;

    @Schema(description = "Course duration in hours (calculated from lessons)", example = "20")
    private Integer duration;

    @Schema(description = "Number of lessons included in the course (configured)", example = "10")
    private Integer numberOfLessons;

    @Schema(description = "Number of lessons actually booked (calculated from lessons list)", example = "5")
    private Integer bookedLessons;

    @Schema(description = "Type of course (THEORETICAL or PRACTICAL)", example = "PRACTICAL")
    private Course.CourseType courseType;

    @Schema(description = "ID of the instructor assigned to this course", example = "1")
    private Long instructorId;

    @Schema(description = "ID of the vehicle assigned to this course", example = "1")
    private Long vehicleId;

    @Schema(description = "Date and time when course was created", example = "2027-01-01T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time when course was last modified", example = "2027-01-01T10:30:00")
    private LocalDateTime lastModifiedDate;
}

