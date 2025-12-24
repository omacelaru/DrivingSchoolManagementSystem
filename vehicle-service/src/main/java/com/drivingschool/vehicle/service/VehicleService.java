package com.drivingschool.vehicle.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.vehicle.dto.VehicleRequest;
import com.drivingschool.vehicle.dto.VehicleResponse;
import com.drivingschool.vehicle.entity.Vehicle;
import com.drivingschool.vehicle.mapper.VehicleMapper;
import com.drivingschool.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public VehicleResponse createVehicle(VehicleRequest request) {
        log.info("Creating vehicle with license plate: {}", request.getLicensePlate());
        
        if (vehicleRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            throw new BusinessException("Vehicle with license plate " + request.getLicensePlate() + " already exists", "DUPLICATE_LICENSE_PLATE");
        }

        Vehicle vehicle = vehicleMapper.toEntity(request);
        vehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created with ID: {}", vehicle.getId());
        
        return vehicleMapper.toResponse(vehicle);
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
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));

        if (!vehicle.getLicensePlate().equals(request.getLicensePlate()) && 
            vehicleRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            throw new BusinessException("Vehicle with license plate " + request.getLicensePlate() + " already exists", "DUPLICATE_LICENSE_PLATE");
        }

        vehicleMapper.updateEntity(vehicle, request);
        vehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle updated with ID: {}", vehicle.getId());
        
        return vehicleMapper.toResponse(vehicle);
    }

    @CacheEvict(value = "vehicles", allEntries = true)
    public List<VehicleResponse> getAvailableVehicles(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Finding available vehicles between {} and {}", startTime, endTime);
        List<Vehicle> vehicles = vehicleRepository.findAvailableVehicles(startTime, endTime);
        return vehicles.stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
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
}

