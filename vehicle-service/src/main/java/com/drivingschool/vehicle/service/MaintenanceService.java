package com.drivingschool.vehicle.service;

import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.vehicle.dto.MaintenanceCreateRequest;
import com.drivingschool.vehicle.dto.MaintenanceRequest;
import com.drivingschool.vehicle.dto.MaintenanceResponse;
import com.drivingschool.vehicle.entity.Maintenance;
import com.drivingschool.vehicle.entity.Vehicle;
import com.drivingschool.vehicle.mapper.MaintenanceMapper;
import com.drivingschool.vehicle.repository.MaintenanceRepository;
import com.drivingschool.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MaintenanceService {

    private static final String VEHICLES_CACHE = "vehicles";

    private final MaintenanceRepository maintenanceRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceMapper maintenanceMapper;
    private final CacheManager cacheManager;

    /**
     * Records maintenance history. Does not change {@link Vehicle.VehicleStatus}; use PUT .../maintenance and .../maintenance/return on vehicles for workflow.
     */
    public MaintenanceResponse createForVehicle(Long vehicleId, MaintenanceRequest request) {
        log.info("Creating maintenance for vehicle ID: {}", vehicleId);
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));
        Maintenance entity = maintenanceMapper.fromRequest(request);
        entity.setVehicle(vehicle);
        entity = maintenanceRepository.save(entity);
        log.info("Maintenance created with ID: {} for vehicle {}", entity.getId(), vehicleId);
        evictVehicleCache(vehicleId);
        return maintenanceMapper.toResponse(entity);
    }

    public MaintenanceResponse create(MaintenanceCreateRequest request) {
        return createForVehicle(request.vehicleId(), request.toMaintenanceRequest());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> listByVehicleId(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle", vehicleId);
        }
        return maintenanceRepository.findByVehicleIdOrderByMaintenanceDateDesc(vehicleId).stream()
                .map(maintenanceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MaintenanceResponse getById(Long maintenanceId) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance", maintenanceId));
        return maintenanceMapper.toResponse(maintenance);
    }

    public MaintenanceResponse update(Long maintenanceId, MaintenanceRequest request) {
        log.info("Updating maintenance ID: {}", maintenanceId);
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance", maintenanceId));
        Long vehicleId = maintenance.getVehicle().getId();
        maintenanceMapper.updateEntity(maintenance, request);
        maintenance = maintenanceRepository.save(maintenance);
        log.info("Maintenance updated with ID: {}", maintenanceId);
        evictVehicleCache(vehicleId);
        return maintenanceMapper.toResponse(maintenance);
    }

    public void delete(Long maintenanceId) {
        log.info("Deleting maintenance ID: {}", maintenanceId);
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance", maintenanceId));
        Long vehicleId = maintenance.getVehicle().getId();
        maintenanceRepository.delete(maintenance);
        log.info("Maintenance deleted with ID: {}", maintenanceId);
        evictVehicleCache(vehicleId);
    }

    private void evictVehicleCache(Long vehicleId) {
        var cache = cacheManager.getCache(VEHICLES_CACHE);
        if (cache != null) {
            cache.evict(vehicleId);
        }
    }
}
