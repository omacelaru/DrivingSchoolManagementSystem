package com.drivingschool.vehicle.dto;

import com.drivingschool.vehicle.entity.Maintenance;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Maintenance record")
public record MaintenanceResponse(
        @Schema(description = "Maintenance id", example = "1")
        Long id,
        @Schema(description = "Vehicle id", example = "1")
        Long vehicleId,
        @Schema(description = "Maintenance date")
        LocalDate maintenanceDate,
        @Schema(description = "Notes")
        String description,
        @Schema(description = "Cost")
        Double cost,
        @Schema(description = "Type")
        Maintenance.MaintenanceType type,
        @Schema(description = "Created at")
        LocalDateTime createdAt
) {
}
