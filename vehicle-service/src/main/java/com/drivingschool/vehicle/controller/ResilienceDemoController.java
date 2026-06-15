package com.drivingschool.vehicle.controller;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.vehicle.service.SchedulingHelperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/resilience-demo")
@RequiredArgsConstructor
@Tag(name = "Resilience Demo", description = "Endpoints for demonstrating Resilience4j Circuit Breakers, Retries, and Fallbacks")
public class ResilienceDemoController {

    private final SchedulingHelperService schedulingHelperService;

    @PostMapping("/scheduling/simulate-failure")
    @Operation(summary = "Enable/disable simulated failures for scheduling-service calls")
    public ResponseEntity<ApiResult<String>> toggleSchedulingFailure(@RequestParam boolean enabled) {
        schedulingHelperService.setSimulateFailure(enabled);
        String state = enabled ? "ENABLED" : "DISABLED";
        return ResponseEntity.ok(ApiResult.success("Simulated scheduling failure is now " + state, state));
    }

    @GetMapping("/scheduling/simulate-failure")
    @Operation(summary = "Get current state of simulated scheduling failures")
    public ResponseEntity<ApiResult<Boolean>> getSchedulingFailureState() {
        return ResponseEntity.ok(ApiResult.success(schedulingHelperService.isSimulateFailure()));
    }

    @PostMapping("/scheduling/test/availability")
    @Operation(summary = "Execute a test call to scheduling-service to observe retries and circuit breaker")
    public ResponseEntity<ApiResult<Boolean>> testSchedulingAvailabilityCall() {
        Boolean result = schedulingHelperService.isVehicleAvailable(
                999L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2)
        );
        return ResponseEntity.ok(ApiResult.success("Vehicle availability check returned: " + result, result));
    }

    @PostMapping("/scheduling/test/assignment")
    @Operation(summary = "Execute a test call for vehicle assignment search to observe retries and circuit breaker")
    public ResponseEntity<ApiResult<Boolean>> testSchedulingAssignmentCall() {
        ApiResult<Boolean> result = schedulingHelperService.fetchVehicleCourseAssignmentExists(999L);
        return ResponseEntity.ok(result);
    }
}
