package com.drivingschool.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Extended student profile returned from the API")
public record StudentProfileResponse(
        @Schema(description = "Emergency contact full name")
        String emergencyContactName,
        @Schema(description = "Emergency contact phone")
        String emergencyContactPhone,
        @Schema(description = "Internal notes")
        String notes
) {
}
