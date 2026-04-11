package com.drivingschool.vehicle.mapper;

import com.drivingschool.common.mapstruct.IgnoreJpaIdAndTimestamps;
import com.drivingschool.vehicle.dto.VehicleRequest;
import com.drivingschool.vehicle.dto.VehicleResponse;
import com.drivingschool.vehicle.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @IgnoreJpaIdAndTimestamps
    @Mapping(target = "status", constant = "AVAILABLE")
    Vehicle toEntity(VehicleRequest request);

    VehicleResponse toResponse(Vehicle vehicle);

    @IgnoreJpaIdAndTimestamps
    @Mapping(target = "status", ignore = true)
    void updateEntity(@MappingTarget Vehicle vehicle, VehicleRequest request);
}
