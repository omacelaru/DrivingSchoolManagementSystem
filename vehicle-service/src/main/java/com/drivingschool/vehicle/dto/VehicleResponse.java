package com.drivingschool.vehicle.dto;

import com.drivingschool.vehicle.entity.Vehicle;
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
public class VehicleResponse {
    private Long id;
    private String licensePlate;
    private String make;
    private String model;
    private Integer year;
    private LocalDate insuranceExpiry;
    private Vehicle.VehicleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedDate;
}

