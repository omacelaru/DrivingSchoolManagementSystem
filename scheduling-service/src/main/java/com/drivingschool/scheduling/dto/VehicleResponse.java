package com.drivingschool.scheduling.dto;

import java.time.LocalDate;

public record VehicleResponse(
    Long id,
    String licensePlate,
    String make,
    String model,
    Integer year,
    LocalDate insuranceExpiry,
    String status
) {
}

