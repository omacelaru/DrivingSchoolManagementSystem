package com.drivingschool.vehicle.mapper;

import com.drivingschool.vehicle.dto.MaintenanceRequest;
import com.drivingschool.vehicle.dto.MaintenanceResponse;
import com.drivingschool.vehicle.entity.Maintenance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MaintenanceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Maintenance fromRequest(MaintenanceRequest request);

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleLicensePlate", source = "vehicle.licensePlate")
    MaintenanceResponse toResponse(Maintenance maintenance);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(@MappingTarget Maintenance maintenance, MaintenanceRequest request);
}
