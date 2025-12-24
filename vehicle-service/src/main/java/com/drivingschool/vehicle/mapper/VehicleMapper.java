package com.drivingschool.vehicle.mapper;

import com.drivingschool.vehicle.dto.VehicleRequest;
import com.drivingschool.vehicle.dto.VehicleResponse;
import com.drivingschool.vehicle.entity.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {
    public Vehicle toEntity(VehicleRequest request) {
        return Vehicle.builder()
                .licensePlate(request.getLicensePlate())
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear())
                .insuranceExpiry(request.getInsuranceExpiry())
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .build();
    }

    public VehicleResponse toResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .licensePlate(vehicle.getLicensePlate())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .insuranceExpiry(vehicle.getInsuranceExpiry())
                .status(vehicle.getStatus())
                .createdAt(vehicle.getCreatedAt())
                .lastModifiedDate(vehicle.getLastModifiedDate())
                .build();
    }

    public void updateEntity(Vehicle vehicle, VehicleRequest request) {
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setInsuranceExpiry(request.getInsuranceExpiry());
    }
}

