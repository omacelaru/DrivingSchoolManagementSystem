package com.drivingschool.vehicle.dto;

import com.drivingschool.vehicle.entity.Maintenance;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Create maintenance with explicit vehicle id (POST /api/maintenances)")
public record MaintenanceCreateRequest(
        @NotNull(message = "Vehicle ID is required")
        @Schema(description = "Vehicle this maintenance belongs to", example = "1")
        Long vehicleId,

        @NotNull(message = "Maintenance date is required")
        @Schema(description = "Date work was performed or scheduled", example = "2026-04-15")
        LocalDate maintenanceDate,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Schema(description = "Notes", example = "Brake inspection")
        String description,

        @NotNull(message = "Cost is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Cost must be zero or positive")
        @Schema(description = "Cost amount", example = "250.00")
        Double cost,

        @NotNull(message = "Maintenance type is required")
        @Schema(description = "Category of work", example = "INSPECTION")
        Maintenance.MaintenanceType type
) {
    public MaintenanceRequest toMaintenanceRequest() {
        return new MaintenanceRequest(maintenanceDate, description, cost, type);
    }
}
