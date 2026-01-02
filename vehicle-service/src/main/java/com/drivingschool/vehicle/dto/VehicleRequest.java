package com.drivingschool.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Request DTO for creating or updating a vehicle")
public class VehicleRequest {
    @NotBlank(message = "License plate is required")
    @Schema(description = "Vehicle license plate number", example = "AB-12-CDE")
    private String licensePlate;

    @NotBlank(message = "Make is required")
    @Schema(description = "Vehicle manufacturer", example = "Toyota")
    private String make;

    @NotBlank(message = "Model is required")
    @Schema(description = "Vehicle model", example = "Corolla")
    private String model;

    @NotNull(message = "Year is required")
    @Schema(description = "Manufacturing year", example = "2020")
    private Integer year;

    @NotNull(message = "Insurance expiry date is required")
    @Schema(description = "Insurance expiration date", example = "2027-12-31")
    private LocalDate insuranceExpiry;
}

