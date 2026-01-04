package com.drivingschool.scheduling.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.scheduling.client.VehicleClient;
import com.drivingschool.scheduling.dto.VehicleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Helper service for vehicle operations with Redis caching and business validations.
 */
@Service
@Slf4j
public class VehicleHelperService {
    private final VehicleClient vehicleClient;

    public VehicleHelperService(VehicleClient vehicleClient) {
        this.vehicleClient = vehicleClient;
    }

    /**
     * Gets vehicle data from vehicle-service with Redis caching (10 min TTL).
     * 
     * @param vehicleId vehicle ID
     * @return vehicle data
     * @throws ResourceNotFoundException if vehicle not found
     */
    @Cacheable(value = "vehicleCache", key = "#vehicleId")
    public VehicleResponse getVehicleOrThrow(Long vehicleId) {
        log.debug("Fetching vehicle with ID: {} from vehicle-service", vehicleId);
        ApiResult<VehicleResponse> vehicleResult = vehicleClient.getVehicleById(vehicleId);
        
        if (vehicleResult == null || vehicleResult.data() == null) {
            log.warn("Vehicle with ID {} not found", vehicleId);
            throw new ResourceNotFoundException("Vehicle", vehicleId);
        }
        
        return vehicleResult.data();
    }

    /**
     * Validates that a vehicle can be used for lessons/courses.
     * Checks:
     * - Vehicle exists
     * - Vehicle status is AVAILABLE
     * - Insurance is not expired
     * 
     * @param vehicleId vehicle ID
     * @throws ResourceNotFoundException if vehicle not found
     * @throws BusinessException if vehicle is not available or insurance is expired
     */
    public void validateVehicleForUse(Long vehicleId) {
        VehicleResponse vehicle = getVehicleOrThrow(vehicleId);
        
        // Check vehicle status
        if (!"AVAILABLE".equalsIgnoreCase(vehicle.status())) {
            throw new BusinessException(
                    String.format("Vehicle with ID %d is not available. Current status: %s", 
                            vehicleId, vehicle.status()),
                    "VEHICLE_NOT_AVAILABLE");
        }
        
        // Check insurance expiry
        if (vehicle.insuranceExpiry() != null && vehicle.insuranceExpiry().isBefore(LocalDate.now())) {
            throw new BusinessException(
                    String.format("Vehicle with ID %d has expired insurance. Expiry date: %s", 
                            vehicleId, vehicle.insuranceExpiry()),
                    "VEHICLE_INSURANCE_EXPIRED");
        }
        
        log.debug("Vehicle ID {} validated for use - status: {}, insurance expires: {}", 
                vehicleId, vehicle.status(), vehicle.insuranceExpiry());
    }
}

