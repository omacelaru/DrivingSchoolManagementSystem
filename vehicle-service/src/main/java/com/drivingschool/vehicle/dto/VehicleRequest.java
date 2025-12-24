package com.drivingschool.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VehicleRequest {
    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotNull(message = "Insurance expiry date is required")
    private LocalDate insuranceExpiry;
}

