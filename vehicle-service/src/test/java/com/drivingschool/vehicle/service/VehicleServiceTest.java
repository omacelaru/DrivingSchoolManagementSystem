package com.drivingschool.vehicle.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.vehicle.client.SchedulingClient;
import com.drivingschool.vehicle.dto.VehicleRequest;
import com.drivingschool.vehicle.dto.VehicleResponse;
import com.drivingschool.vehicle.entity.Maintenance;
import com.drivingschool.vehicle.entity.Vehicle;
import com.drivingschool.vehicle.fixture.VehicleFixture;
import com.drivingschool.vehicle.mapper.VehicleMapper;
import com.drivingschool.vehicle.repository.MaintenanceRepository;
import com.drivingschool.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    private final VehicleMapper vehicleMapper = new VehicleMapper();

    @Mock
    private SchedulingClient schedulingClient;

    @Mock
    private MaintenanceRepository maintenanceRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleRequest vehicleRequest;
    private Vehicle vehicle;


    @BeforeEach
    void setUp() {
        vehicleRequest = VehicleFixture.vehicleRequest();
        vehicle = VehicleFixture.vehicleAvailable();
        vehicleService = new VehicleService(
                vehicleRepository,
                vehicleMapper,
                schedulingClient,
                maintenanceRepository
        );
    }

    @Test
    void testCreateVehicle_Success() {
        // Given
        when(vehicleRepository.findByLicensePlate(VehicleFixture.defaultLicensePlate())).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // When
        VehicleResponse result = vehicleService.createVehicle(vehicleRequest);

        // Then
        assertNotNull(result);
        assertEquals(VehicleFixture.defaultLicensePlate(), result.licensePlate());
        assertEquals(VehicleFixture.defaultVehicleId(), result.id());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    void testCreateVehicle_DuplicateLicensePlate() {
        // Given
        when(vehicleRepository.findByLicensePlate(VehicleFixture.defaultLicensePlate())).thenReturn(Optional.of(vehicle));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            vehicleService.createVehicle(vehicleRequest);
        });

        assertEquals("DUPLICATE_LICENSE_PLATE", exception.getErrorCode());
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void testGetVehicleById_Success() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        // When
        VehicleResponse result = vehicleService.getVehicleById(vehicleId);

        // Then
        assertNotNull(result);
        assertEquals(vehicleId, result.id());
    }

    @Test
    void testGetVehicleById_NotFound() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            vehicleService.getVehicleById(vehicleId);
        });
    }

    @Test
    void testUpdateVehicle_Success() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        String updatedMake = "Honda";
        String updatedModel = "Civic";
        Integer updatedYear = 2021;
        
        VehicleRequest updateRequest = new VehicleRequest(
                VehicleFixture.defaultLicensePlate(),
                updatedMake,
                updatedModel,
                updatedYear,
                VehicleFixture.defaultInsuranceExpiry()
        );

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        VehicleResponse result = vehicleService.updateVehicle(vehicleId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(updatedMake, result.make());
        assertEquals(updatedModel, result.model());
        assertEquals(updatedYear, result.year());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    void testUpdateVehicle_NotFound() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            vehicleService.updateVehicle(vehicleId, vehicleRequest);
        });
    }

    @Test
    void testUpdateVehicle_DuplicateLicensePlate() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        Long existingVehicleId = 2L;
        String duplicateLicensePlate = "XY-99-ZZZ";
        
        VehicleRequest updateRequest = VehicleFixture.vehicleRequest(duplicateLicensePlate);
        Vehicle existingVehicle = VehicleFixture.vehicle(existingVehicleId, Vehicle.VehicleStatus.AVAILABLE);
        existingVehicle.setLicensePlate(duplicateLicensePlate);

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.findByLicensePlate(duplicateLicensePlate)).thenReturn(Optional.of(existingVehicle));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            vehicleService.updateVehicle(vehicleId, updateRequest);
        });

        assertEquals("DUPLICATE_LICENSE_PLATE", exception.getErrorCode());
    }

    @Test
    void testGetAvailableVehicles_Success() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);

        List<Vehicle> availableVehicles = Collections.singletonList(vehicle);
        when(vehicleRepository.findByStatus(Vehicle.VehicleStatus.AVAILABLE)).thenReturn(availableVehicles);
        when(schedulingClient.isVehicleAvailable(vehicleId, startTime, endTime)).thenReturn(true);

        // When
        List<VehicleResponse> result = vehicleService.getAvailableVehicles(startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(vehicleId, result.get(0).id());
    }

    @Test
    void testGetAvailableVehicles_FiltersOutScheduledVehicles() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);

        List<Vehicle> availableVehicles = Collections.singletonList(vehicle);
        when(vehicleRepository.findByStatus(Vehicle.VehicleStatus.AVAILABLE)).thenReturn(availableVehicles);
        when(schedulingClient.isVehicleAvailable(vehicleId, startTime, endTime)).thenReturn(false);

        // When
        List<VehicleResponse> result = vehicleService.getAvailableVehicles(startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetAvailableVehicles_HandlesSchedulingClientError() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        String errorMessage = "Service unavailable";

        List<Vehicle> availableVehicles = Collections.singletonList(vehicle);
        when(vehicleRepository.findByStatus(Vehicle.VehicleStatus.AVAILABLE)).thenReturn(availableVehicles);
        when(schedulingClient.isVehicleAvailable(vehicleId, startTime, endTime))
                .thenThrow(new RuntimeException(errorMessage));

        // When
        List<VehicleResponse> result = vehicleService.getAvailableVehicles(startTime, endTime);

        // Then - Should return vehicle as available (fail-safe)
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(vehicleId, result.get(0).id());
    }

    @Test
    void testGetAllVehicles_WithStatus() {
        // Given
        List<Vehicle> vehicles = Collections.singletonList(vehicle);
        when(vehicleRepository.findByStatus(Vehicle.VehicleStatus.AVAILABLE)).thenReturn(vehicles);

        // When
        List<VehicleResponse> result = vehicleService.getAllVehicles(Vehicle.VehicleStatus.AVAILABLE);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllVehicles_WithoutStatus() {
        // Given
        List<Vehicle> vehicles = Collections.singletonList(vehicle);
        when(vehicleRepository.findAll()).thenReturn(vehicles);

        // When
        List<VehicleResponse> result = vehicleService.getAllVehicles(null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testSendToMaintenance_Success() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        Long maintenanceId = 1L;
        
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle saved = invocation.getArgument(0);
            saved.setStatus(Vehicle.VehicleStatus.MAINTENANCE);
            return saved;
        });
        when(maintenanceRepository.save(any(Maintenance.class)))
                .thenReturn(Maintenance.builder().id(maintenanceId).build());

        // When
        VehicleResponse result = vehicleService.sendToMaintenance(vehicleId);

        // Then
        assertNotNull(result);
        assertEquals(Vehicle.VehicleStatus.MAINTENANCE, result.status());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(maintenanceRepository, times(1)).save(any(Maintenance.class));
    }

    @Test
    void testSendToMaintenance_NotFound() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            vehicleService.sendToMaintenance(vehicleId);
        });
    }

    @Test
    void testSendToMaintenance_AlreadyInMaintenance() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        Vehicle vehicleInMaintenance = VehicleFixture.vehicleInMaintenance();

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicleInMaintenance));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            vehicleService.sendToMaintenance(vehicleId);
        });

        assertEquals("VEHICLE_ALREADY_IN_MAINTENANCE", exception.getErrorCode());
    }

    @Test
    void testReturnFromMaintenance_Success() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        Vehicle vehicleInMaintenance = VehicleFixture.vehicleInMaintenance();

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicleInMaintenance));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle saved = invocation.getArgument(0);
            saved.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            return saved;
        });

        // When
        VehicleResponse result = vehicleService.returnFromMaintenance(vehicleId);

        // Then
        assertNotNull(result);
        assertEquals(Vehicle.VehicleStatus.AVAILABLE, result.status());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    void testReturnFromMaintenance_NotFound() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            vehicleService.returnFromMaintenance(vehicleId);
        });
    }

    @Test
    void testReturnFromMaintenance_NotInMaintenance() {
        // Given
        Long vehicleId = VehicleFixture.defaultVehicleId();
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            vehicleService.returnFromMaintenance(vehicleId);
        });

        assertEquals("VEHICLE_NOT_IN_MAINTENANCE", exception.getErrorCode());
    }
}
