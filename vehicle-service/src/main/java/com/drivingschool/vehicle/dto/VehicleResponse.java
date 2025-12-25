package com.drivingschool.vehicle.dto;

import com.drivingschool.vehicle.entity.Vehicle;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public VehicleResponse() {
    }

    public VehicleResponse(Long id, String licensePlate, String make, String model, Integer year, LocalDate insuranceExpiry, Vehicle.VehicleStatus status, LocalDateTime createdAt, LocalDateTime lastModifiedDate) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.make = make;
        this.model = model;
        this.year = year;
        this.insuranceExpiry = insuranceExpiry;
        this.status = status;
        this.createdAt = createdAt;
        this.lastModifiedDate = lastModifiedDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public LocalDate getInsuranceExpiry() {
        return insuranceExpiry;
    }

    public void setInsuranceExpiry(LocalDate insuranceExpiry) {
        this.insuranceExpiry = insuranceExpiry;
    }

    public Vehicle.VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(Vehicle.VehicleStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
