package com.drivingschool.vehicle.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.vehicle.dto.VehicleRequest;
import com.drivingschool.vehicle.entity.Vehicle;
import com.drivingschool.vehicle.mapper.VehicleMapper;
import com.drivingschool.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleRequest vehicleRequest;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicleRequest = new VehicleRequest();
        vehicleRequest.setLicensePlate("AB-12-CDE");
        vehicleRequest.setMake("Toyota");
        vehicleRequest.setModel("Corolla");
        vehicleRequest.setYear(2020);
        vehicleRequest.setInsuranceExpiry(LocalDate.now().plusYears(1));

        vehicle = Vehicle.builder()
                .id(1L)
                .licensePlate("AB-12-CDE")
                .make("Toyota")
                .model("Corolla")
                .year(2020)
                .insuranceExpiry(LocalDate.now().plusYears(1))
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .build();
    }

    @Test
    void testCreateVehicle_Success() {
        when(vehicleRepository.findByLicensePlate(anyString())).thenReturn(Optional.empty());
        when(vehicleMapper.toEntity(any(VehicleRequest.class))).thenReturn(vehicle);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(vehicleMapper.toResponse(any(Vehicle.class))).thenReturn(null);

        assertDoesNotThrow(() -> {
            vehicleService.createVehicle(vehicleRequest);
        });

        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    void testCreateVehicle_DuplicateLicensePlate() {
        when(vehicleRepository.findByLicensePlate(anyString())).thenReturn(Optional.of(vehicle));

        assertThrows(BusinessException.class, () -> {
            vehicleService.createVehicle(vehicleRequest);
        });
    }

    @Test
    void testGetVehicleById_NotFound() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            vehicleService.getVehicleById(1L);
        });
    }
}

