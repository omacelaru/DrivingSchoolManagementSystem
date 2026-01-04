package com.drivingschool.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request DTO for creating or updating a vehicle")
public record VehicleRequest(
    @NotBlank(message = "License plate is required")
    @Size(max = 20, message = "License plate must not exceed 20 characters")
    @Pattern(regexp = "^[A-Z]{1,3}-[0-9]{2,3}-[A-Z]{2,3}$", message = "License plate must be in Romanian format (e.g., B-123-ABC or AB-12-CDE)")
    @Schema(description = "Vehicle license plate number", example = "AB-12-CDE")
    String licensePlate,

    @NotBlank(message = "Make is required")
    @Size(max = 50, message = "Make must not exceed 50 characters")
    @Schema(description = "Vehicle manufacturer", example = "Toyota")
    String make,

    @NotBlank(message = "Model is required")
    @Size(max = 50, message = "Model must not exceed 50 characters")
    @Schema(description = "Vehicle model", example = "Corolla")
    String model,

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be at least 1900")
    @Max(value = 2100, message = "Year must not exceed 2100")
    @Schema(description = "Manufacturing year", example = "2020")
    Integer year,

    @NotNull(message = "Insurance expiry date is required")
    @Future(message = "Insurance expiry date must be in the future")
    @Schema(description = "Insurance expiration date", example = "2027-12-31")
    LocalDate insuranceExpiry
) {
}

