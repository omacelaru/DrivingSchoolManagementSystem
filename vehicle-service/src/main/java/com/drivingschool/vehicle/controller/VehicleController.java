package com.drivingschool.vehicle.controller;

import com.drivingschool.common.dto.ApiResponse;
import com.drivingschool.vehicle.dto.VehicleRequest;
import com.drivingschool.vehicle.dto.VehicleResponse;
import com.drivingschool.vehicle.entity.Vehicle;
import com.drivingschool.vehicle.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicle Management", description = "APIs for managing vehicles")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @Operation(summary = "Register a new vehicle", description = "Creates a new vehicle in the fleet")
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle registered successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Retrieves vehicle details")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle information", description = "Updates vehicle details")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all vehicles", description = "Retrieves all vehicles, optionally filtered by status")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllVehicles(
            @Parameter(description = "Filter by status") @RequestParam(required = false) Vehicle.VehicleStatus status) {
        List<VehicleResponse> vehicles = vehicleService.getAllVehicles(status);
        return ResponseEntity.ok(ApiResponse.success(vehicles));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available vehicles", description = "Finds vehicles available for a time slot")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAvailableVehicles(
            @Parameter(description = "Start time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<VehicleResponse> vehicles = vehicleService.getAvailableVehicles(startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(vehicles));
    }
}

