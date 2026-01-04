package com.drivingschool.vehicle.mapper;

import com.drivingschool.vehicle.dto.VehicleRequest;
import com.drivingschool.vehicle.dto.VehicleResponse;
import com.drivingschool.vehicle.entity.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {
    public Vehicle toEntity(VehicleRequest request) {
        return Vehicle.builder()
                .licensePlate(request.licensePlate())
                .make(request.make())
                .model(request.model())
                .year(request.year())
                .insuranceExpiry(request.insuranceExpiry())
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .build();
    }

    public VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getInsuranceExpiry(),
                vehicle.getStatus(),
                vehicle.getCreatedAt(),
                vehicle.getLastModifiedDate()
        );
    }

    public void updateEntity(Vehicle vehicle, VehicleRequest request) {
        vehicle.setLicensePlate(request.licensePlate());
        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setYear(request.year());
        vehicle.setInsuranceExpiry(request.insuranceExpiry());
    }
}

