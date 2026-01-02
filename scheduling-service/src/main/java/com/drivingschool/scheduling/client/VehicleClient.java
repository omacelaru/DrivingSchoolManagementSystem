package com.drivingschool.scheduling.client;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.scheduling.dto.VehicleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "vehicle-service", url = "${vehicle.service.url}")
public interface VehicleClient {
    @GetMapping("/api/vehicles/{id}")
    ApiResult<VehicleResponse> getVehicleById(@PathVariable Long id);
}

