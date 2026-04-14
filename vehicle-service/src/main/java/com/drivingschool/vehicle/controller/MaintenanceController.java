package com.drivingschool.vehicle.controller;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.vehicle.dto.MaintenanceCreateRequest;
import com.drivingschool.vehicle.dto.MaintenanceRequest;
import com.drivingschool.vehicle.dto.MaintenanceResponse;
import com.drivingschool.vehicle.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenances")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "CRUD for maintenance records. Does not move vehicle status; use Vehicle send/return maintenance for MAINTENANCE ↔ AVAILABLE workflow.")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping
    @Operation(summary = "Create maintenance record", description = "Links a maintenance row to a vehicle. Prefer POST /api/vehicles/{id}/maintenances when the vehicle is known from context.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(schema = @Schema(implementation = MaintenanceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<ApiResult<MaintenanceResponse>> create(
            @Valid @RequestBody MaintenanceCreateRequest request) {
        MaintenanceResponse response = maintenanceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("Maintenance record created", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get maintenance by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(schema = @Schema(implementation = MaintenanceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResult<MaintenanceResponse>> getById(
            @Parameter(description = "Maintenance id", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResult.success(maintenanceService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update maintenance record", description = "Updates date, description, cost, and type. Vehicle link is unchanged.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated",
                    content = @Content(schema = @Schema(implementation = MaintenanceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResult<MaintenanceResponse>> update(
            @Parameter(description = "Maintenance id", required = true) @PathVariable Long id,
            @Valid @RequestBody MaintenanceRequest request) {
        MaintenanceResponse response = maintenanceService.update(id, request);
        return ResponseEntity.ok(ApiResult.success("Maintenance record updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete maintenance record", description = "Removes history row only; use vehicle APIs to change operational status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResult<Void>> delete(
            @Parameter(description = "Maintenance id", required = true) @PathVariable Long id) {
        maintenanceService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResult.success("Maintenance record deleted", null));
    }
}
