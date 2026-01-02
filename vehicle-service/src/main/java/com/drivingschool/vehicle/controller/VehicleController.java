package com.drivingschool.vehicle.controller;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.vehicle.dto.VehicleRequest;
import com.drivingschool.vehicle.dto.VehicleResponse;
import com.drivingschool.vehicle.entity.Vehicle;
import com.drivingschool.vehicle.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle Management", description = "APIs for managing vehicles in the fleet, including registration, updates, status management, and availability checking")
public class VehicleController {
    private final VehicleService vehicleService;

    @PostMapping
    @Operation(summary = "Register a new vehicle", 
              description = "Creates a new vehicle in the fleet. Validates license plate uniqueness and insurance expiry date.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Vehicle registered successfully",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation failed"),
        @ApiResponse(responseCode = "409", description = "Vehicle with this license plate already exists")
    })
    public ResponseEntity<ApiResult<VehicleResponse>> createVehicle(
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("Vehicle registered successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", 
              description = "Retrieves detailed information about a specific vehicle, including status and insurance information.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehicle found",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
        @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<ApiResult<VehicleResponse>> getVehicle(
            @Parameter(description = "Unique vehicle identifier", example = "1", required = true) 
            @PathVariable Long id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle information", 
              description = "Updates existing vehicle details. Can be used to update insurance expiry, status, or other vehicle information.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehicle updated successfully",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<ApiResult<VehicleResponse>> updateVehicle(
            @Parameter(description = "Unique vehicle identifier", example = "1", required = true) 
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResult.success("Vehicle updated successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all vehicles", 
              description = "Retrieves a list of all vehicles in the fleet. Can be optionally filtered by status (AVAILABLE, IN_USE, MAINTENANCE, RETIRED).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of vehicles retrieved successfully")
    })
    public ResponseEntity<ApiResult<List<VehicleResponse>>> getAllVehicles(
            @Parameter(description = "Filter by vehicle status (AVAILABLE, IN_USE, MAINTENANCE, RETIRED)", example = "AVAILABLE") 
            @RequestParam(required = false) Vehicle.VehicleStatus status) {
        List<VehicleResponse> vehicles = vehicleService.getAllVehicles(status);
        return ResponseEntity.ok(ApiResult.success(vehicles));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available vehicles", 
              description = "Finds all vehicles with AVAILABLE status.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Available vehicles retrieved successfully")
    })
    public ResponseEntity<ApiResult<List<VehicleResponse>>> getAvailableVehicles() {
        List<VehicleResponse> vehicles = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(ApiResult.success(vehicles));
    }
}

