package com.drivingschool.vehicle.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.vehicle.client.SchedulingClient;
import com.drivingschool.vehicle.dto.VehicleRequest;
import com.drivingschool.vehicle.dto.VehicleResponse;
import com.drivingschool.vehicle.entity.Maintenance;
import com.drivingschool.vehicle.entity.Vehicle;
import com.drivingschool.vehicle.mapper.VehicleMapper;
import com.drivingschool.vehicle.repository.MaintenanceRepository;
import com.drivingschool.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final SchedulingClient schedulingClient;
    private final MaintenanceRepository maintenanceRepository;

    public VehicleResponse createVehicle(VehicleRequest request) {
        log.info("Creating vehicle with license plate: {}", request.licensePlate());
        
        validateLicensePlateUniqueness(request.licensePlate());
        Vehicle vehicle = vehicleMapper.toEntity(request);
        vehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created with ID: {}", vehicle.getId());
        
        return vehicleMapper.toResponse(vehicle);
    }

    private void validateLicensePlateUniqueness(String licensePlate) {
        if (vehicleRepository.findByLicensePlate(licensePlate).isPresent()) {
            throw new BusinessException("Vehicle with license plate " + licensePlate + " already exists", ErrorCode.DUPLICATE_LICENSE_PLATE);
        }
    }

    @Cacheable(value = "vehicles", key = "#id")
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        log.info("Fetching vehicle with ID: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
        return vehicleMapper.toResponse(vehicle);
    }

    @CacheEvict(value = "vehicles", key = "#id")
    public VehicleResponse updateVehicle(Long id, VehicleRequest request) {
        log.info("Updating vehicle with ID: {}", id);
        Vehicle vehicle = findVehicleById(id);
        validateLicensePlateUniquenessForUpdate(vehicle, request.licensePlate());
        vehicleMapper.updateEntity(vehicle, request);
        vehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle updated with ID: {}", vehicle.getId());
        
        return vehicleMapper.toResponse(vehicle);
    }

    private Vehicle findVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }

    private void validateLicensePlateUniquenessForUpdate(Vehicle existingVehicle, String newLicensePlate) {
        if (!existingVehicle.getLicensePlate().equals(newLicensePlate) && 
            vehicleRepository.findByLicensePlate(newLicensePlate).isPresent()) {
            throw new BusinessException("Vehicle with license plate " + newLicensePlate + " already exists", ErrorCode.DUPLICATE_LICENSE_PLATE);
        }
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getAvailableVehicles(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Finding available vehicles between {} and {}", startTime, endTime);
        List<Vehicle> availableVehicles = vehicleRepository.findByStatus(Vehicle.VehicleStatus.AVAILABLE);
        
        return availableVehicles.stream()
                .filter(vehicle -> isVehicleAvailableForScheduling(vehicle.getId(), startTime, endTime))
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
    }

    private boolean isVehicleAvailableForScheduling(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Boolean isAvailable = schedulingClient.isVehicleAvailable(vehicleId, startTime, endTime);
            if (!Boolean.TRUE.equals(isAvailable)) {
                log.debug("Vehicle ID {} is not available - has scheduled lessons", vehicleId);
            }
            return Boolean.TRUE.equals(isAvailable);
        } catch (Exception e) {
            log.warn("Failed to check availability for vehicle ID {}: {}", vehicleId, e.getMessage());
            // In case of error, assume vehicle is available (fail-safe)
            return true;
        }
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles(Vehicle.VehicleStatus status) {
        log.info("Fetching all vehicles with status: {}", status);
        List<Vehicle> vehicles = status != null 
                ? vehicleRepository.findByStatus(status)
                : vehicleRepository.findAll();
        return vehicles.stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "vehicles", key = "#id")
    public VehicleResponse sendToMaintenance(Long id) {
        log.info("Sending vehicle with ID: {} to maintenance", id);
        Vehicle vehicle = findVehicleById(id);
        validateVehicleNotAlreadyInMaintenance(vehicle);
        vehicle.setStatus(Vehicle.VehicleStatus.MAINTENANCE);
        vehicle = vehicleRepository.save(vehicle);

        Maintenance maintenance = Maintenance.builder()
                .vehicle(vehicle)
                .maintenanceDate(LocalDate.now())
                .cost(0.0)
                .type(Maintenance.MaintenanceType.OTHER)
                .description("Vehicle sent to maintenance")
                .build();
        maintenanceRepository.save(maintenance);
        log.info("Maintenance entry created for vehicle ID: {}", vehicle.getId());
        
        log.info("Vehicle with ID: {} sent to maintenance", vehicle.getId());

        return vehicleMapper.toResponse(vehicle);
    }

    @CacheEvict(value = "vehicles", key = "#id")
    public VehicleResponse returnFromMaintenance(Long id) {
        log.info("Returning vehicle with ID: {} from maintenance", id);
        Vehicle vehicle = findVehicleById(id);
        validateVehicleIsInMaintenance(vehicle);
        vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
        vehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle with ID: {} returned from maintenance and is now AVAILABLE", vehicle.getId());

        return vehicleMapper.toResponse(vehicle);
    }

    private void validateVehicleNotAlreadyInMaintenance(Vehicle vehicle) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.MAINTENANCE) {
            throw new BusinessException("Vehicle is already in maintenance", ErrorCode.VEHICLE_ALREADY_IN_MAINTENANCE);
        }
    }

    private void validateVehicleIsInMaintenance(Vehicle vehicle) {
        if (vehicle.getStatus() != Vehicle.VehicleStatus.MAINTENANCE) {
            throw new BusinessException("Vehicle is not in maintenance", ErrorCode.VEHICLE_NOT_IN_MAINTENANCE);
        }
    }
}

