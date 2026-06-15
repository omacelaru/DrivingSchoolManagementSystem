package com.drivingschool.vehicle.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.exception.ResilienceDemoException;
import com.drivingschool.vehicle.client.SchedulingClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingHelperService {

    private final SchedulingClient schedulingClient;
    @Getter
    @Setter
    private boolean simulateFailure = false;

    @CircuitBreaker(name = "schedulingService")
    @Retry(name = "schedulingService", fallbackMethod = "fetchVehicleCourseAssignmentExistsFallback")
    public ApiResult<Boolean> fetchVehicleCourseAssignmentExists(Long vehicleId) {
        if (simulateFailure) {
            log.warn("Simulating scheduling-service - fetchVehicleCourseAssignmentExists failure (as requested)...");
            throw new ResilienceDemoException("Simulated scheduling-service failure");
        }
        log.info("Checking course assignment for vehicle ID: {} using scheduling-service", vehicleId);
        return schedulingClient.fetchVehicleCourseAssignmentExists(vehicleId);
    }

    @CircuitBreaker(name = "schedulingService")
    @Retry(name = "schedulingService", fallbackMethod = "isVehicleAvailableFallback")
    public Boolean isVehicleAvailable(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        if (simulateFailure) {
            log.warn("Simulating scheduling-service - isVehicleAvailable failure (as requested)...");
            throw new ResilienceDemoException("Simulated scheduling-service failure");
        }
        log.info("Checking availability for vehicle ID: {} using scheduling-service", vehicleId);
        return schedulingClient.isVehicleAvailable(vehicleId, startTime, endTime);
    }

    // Fallback for fetchVehicleCourseAssignmentExists
    public ApiResult<Boolean> fetchVehicleCourseAssignmentExistsFallback(Long vehicleId, Throwable t) {
        log.error("Fallback invoked for fetchVehicleCourseAssignmentExists. scheduling-service failed: {}", t.getMessage());
        // Return true (which blocks vehicle deletion to prevent data integrity issues)
        return ApiResult.success("Fallback: assuming vehicle course assignment exists to prevent accidental deletion", true);
    }

    // Fallback for isVehicleAvailable
    public Boolean isVehicleAvailableFallback(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime, Throwable t) {
        log.error("Fallback invoked for isVehicleAvailable. scheduling-service failed: {}", t.getMessage());
        // Fail-safe: assume available so user operations are not blocked
        return true;
    }
}
