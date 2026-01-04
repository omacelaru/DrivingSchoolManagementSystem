package com.drivingschool.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Request DTO for creating or updating a vehicle")
public record VehicleRequest(
    @NotBlank(message = "License plate is required")
    @Schema(description = "Vehicle license plate number", example = "AB-12-CDE")
    String licensePlate,

    @NotBlank(message = "Make is required")
    @Schema(description = "Vehicle manufacturer", example = "Toyota")
    String make,

    @NotBlank(message = "Model is required")
    @Schema(description = "Vehicle model", example = "Corolla")
    String model,

    @NotNull(message = "Year is required")
    @Schema(description = "Manufacturing year", example = "2020")
    Integer year,

    @NotNull(message = "Insurance expiry date is required")
    @Schema(description = "Insurance expiration date", example = "2027-12-31")
    LocalDate insuranceExpiry
) {
}

