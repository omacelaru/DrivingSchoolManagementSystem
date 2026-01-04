package com.drivingschool.vehicle.dto;

import com.drivingschool.vehicle.entity.Vehicle;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Response DTO containing vehicle information")
public record VehicleResponse(
    @Schema(description = "Unique vehicle identifier", example = "1")
    Long id,
    
    @Schema(description = "Vehicle license plate number", example = "AB-12-CDE")
    String licensePlate,
    
    @Schema(description = "Vehicle manufacturer", example = "Toyota")
    String make,
    
    @Schema(description = "Vehicle model", example = "Corolla")
    String model,
    
    @Schema(description = "Manufacturing year", example = "2020")
    Integer year,
    
    @Schema(description = "Insurance expiration date", example = "2027-12-31")
    LocalDate insuranceExpiry,
    
    @Schema(description = "Current vehicle status", example = "AVAILABLE")
    Vehicle.VehicleStatus status,
    
    @Schema(description = "Date and time when vehicle was registered", example = "2027-01-01T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "Date and time when vehicle information was last modified", example = "2027-01-01T14:45:00")
    LocalDateTime lastModifiedDate
) {
}

