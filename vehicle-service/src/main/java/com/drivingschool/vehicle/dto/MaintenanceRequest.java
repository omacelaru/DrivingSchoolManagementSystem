package com.drivingschool.vehicle.dto;

import com.drivingschool.vehicle.entity.Maintenance;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Create or update a maintenance record (vehicle comes from URL when nested under /vehicles/{id}/maintenances)")
public record MaintenanceRequest(
        @NotNull(message = "Maintenance date is required")
        @Schema(description = "Date work was performed or scheduled", example = "2026-04-15")
        LocalDate maintenanceDate,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Schema(description = "Notes", example = "Oil change")
        String description,

        @NotNull(message = "Cost is required")
        @DecimalMin(value = "0.0", message = "Cost must be zero or positive")
        @Schema(description = "Cost amount", example = "150.00")
        Double cost,

        @NotNull(message = "Maintenance type is required")
        @Schema(description = "Category of work", example = "ROUTINE")
        Maintenance.MaintenanceType type
) {
}
