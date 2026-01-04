package com.drivingschool.vehicle.client;

import com.drivingschool.common.dto.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@FeignClient(name = "scheduling-service", url = "${scheduling.service.url}")
public interface SchedulingClient {
    
    /**
     * Checks if a vehicle is available for a specific time slot.
     * 
     * @param vehicleId vehicle ID
     * @param startTime start time (ISO format string, required)
     * @param endTime end time (ISO format string, required)
     * @return ApiResult containing true if vehicle is available, false otherwise
     */
    @GetMapping("/api/lessons/vehicles/{vehicleId}/availability")
    ApiResult<Boolean> checkVehicleAvailability(
            @PathVariable Long vehicleId,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime);
    
    /**
     * Helper method to check vehicle availability for a specific time slot.
     * 
     * @param vehicleId vehicle ID
     * @param startTime start time
     * @param endTime end time
     * @return true if vehicle is available, false otherwise
     */
    default Boolean isVehicleAvailable(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        ApiResult<Boolean> result = checkVehicleAvailability(
                vehicleId, 
                startTime.format(formatter), 
                endTime.format(formatter));
        return result != null && result.data() != null ? result.data() : false;
    }
}

