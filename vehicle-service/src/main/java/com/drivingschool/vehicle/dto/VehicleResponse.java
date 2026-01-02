package com.drivingschool.vehicle.dto;

import com.drivingschool.vehicle.entity.Vehicle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing vehicle information")
public class VehicleResponse {
    @Schema(description = "Unique vehicle identifier", example = "1")
    private Long id;
    
    @Schema(description = "Vehicle license plate number", example = "AB-12-CDE")
    private String licensePlate;
    
    @Schema(description = "Vehicle manufacturer", example = "Toyota")
    private String make;
    
    @Schema(description = "Vehicle model", example = "Corolla")
    private String model;
    
    @Schema(description = "Manufacturing year", example = "2020")
    private Integer year;
    
    @Schema(description = "Insurance expiration date", example = "2027-12-31")
    private LocalDate insuranceExpiry;
    
    @Schema(description = "Current vehicle status", example = "AVAILABLE")
    private Vehicle.VehicleStatus status;
    
    @Schema(description = "Date and time when vehicle was registered", example = "2027-01-01T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time when vehicle information was last modified", example = "2027-01-01T14:45:00")
    private LocalDateTime lastModifiedDate;
}

