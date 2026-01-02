package com.drivingschool.scheduling.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VehicleResponse {
    private Long id;
    private String licensePlate;
    private String make;
    private String model;
    private Integer year;
    private LocalDate insuranceExpiry;
    private String status;
}

