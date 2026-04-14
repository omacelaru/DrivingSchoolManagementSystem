package com.drivingschool.vehicle.service;

import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.vehicle.dto.MaintenanceCreateRequest;
import com.drivingschool.vehicle.dto.MaintenanceRequest;
import com.drivingschool.vehicle.dto.MaintenanceResponse;
import com.drivingschool.vehicle.entity.Maintenance;
import com.drivingschool.vehicle.entity.Vehicle;
import com.drivingschool.vehicle.fixture.VehicleFixture;
import com.drivingschool.vehicle.mapper.MaintenanceMapper;
import com.drivingschool.vehicle.repository.MaintenanceRepository;
import com.drivingschool.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private MaintenanceRepository maintenanceRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private final MaintenanceMapper maintenanceMapper = Mappers.getMapper(MaintenanceMapper.class);

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache vehiclesCache;

    private MaintenanceService maintenanceService;

    @BeforeEach
    void setUp() {
        maintenanceService = new MaintenanceService(
                maintenanceRepository,
                vehicleRepository,
                maintenanceMapper,
                cacheManager);
        lenient().when(cacheManager.getCache("vehicles")).thenReturn(vehiclesCache);
    }

    @Test
    void whenCreateForVehicle_thenSavesAndReturnsResponse() {
        Long vehicleId = VehicleFixture.defaultVehicleId();
        Vehicle vehicle = VehicleFixture.vehicleAvailable();
        MaintenanceRequest request = new MaintenanceRequest(
                LocalDate.now(), "Oil change", 99.0, Maintenance.MaintenanceType.ROUTINE);

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(inv -> {
            Maintenance m = inv.getArgument(0);
            m.setId(10L);
            m.setCreatedAt(LocalDateTime.now());
            return m;
        });

        MaintenanceResponse result = maintenanceService.createForVehicle(vehicleId, request);

        assertNotNull(result);
        assertEquals(10L, result.id());
        assertEquals(vehicleId, result.vehicleId());
        verify(maintenanceRepository).save(any(Maintenance.class));
        verify(vehiclesCache).evict(vehicleId);
    }

    @Test
    void whenCreateForVehicleVehicleMissing_thenThrows() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());
        MaintenanceRequest request = new MaintenanceRequest(
                LocalDate.now(), "x", 0.0, Maintenance.MaintenanceType.OTHER);

        assertThrows(ResourceNotFoundException.class,
                () -> maintenanceService.createForVehicle(1L, request));
        verify(maintenanceRepository, never()).save(any());
    }

    @Test
    void whenCreateWithCreateRequest_thenDelegates() {
        MaintenanceCreateRequest req = new MaintenanceCreateRequest(
                VehicleFixture.defaultVehicleId(),
                LocalDate.now(),
                "Brakes",
                200.0,
                Maintenance.MaintenanceType.REPAIR);
        Vehicle vehicle = VehicleFixture.vehicleAvailable();
        when(vehicleRepository.findById(VehicleFixture.defaultVehicleId())).thenReturn(Optional.of(vehicle));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(inv -> {
            Maintenance m = inv.getArgument(0);
            m.setId(3L);
            m.setCreatedAt(LocalDateTime.now());
            return m;
        });

        MaintenanceResponse result = maintenanceService.create(req);

        assertEquals(3L, result.id());
        verify(vehiclesCache).evict(VehicleFixture.defaultVehicleId());
    }

    @Test
    void whenListByVehicle_thenReturnsOrderedStub() {
        Long vehicleId = VehicleFixture.defaultVehicleId();
        Vehicle vehicle = VehicleFixture.vehicleAvailable();
        Maintenance m = VehicleFixture.maintenance();
        m.setCreatedAt(LocalDateTime.now());
        when(vehicleRepository.existsById(vehicleId)).thenReturn(true);
        when(maintenanceRepository.findByVehicleIdOrderByMaintenanceDateDesc(vehicleId)).thenReturn(List.of(m));

        List<MaintenanceResponse> list = maintenanceService.listByVehicleId(vehicleId);

        assertEquals(1, list.size());
        assertEquals(vehicle.getId(), list.getFirst().vehicleId());
    }

    @Test
    void whenGetById_thenReturns() {
        Maintenance m = VehicleFixture.maintenance();
        m.setCreatedAt(LocalDateTime.now());
        when(maintenanceRepository.findById(1L)).thenReturn(Optional.of(m));

        MaintenanceResponse r = maintenanceService.getById(1L);

        assertEquals(1L, r.id());
    }

    @Test
    void whenUpdate_thenSavesAndEvicts() {
        Maintenance m = VehicleFixture.maintenance();
        m.setCreatedAt(LocalDateTime.now());
        MaintenanceRequest update = new MaintenanceRequest(
                LocalDate.now().minusDays(1), "Updated", 50.0, Maintenance.MaintenanceType.INSPECTION);
        when(maintenanceRepository.findById(1L)).thenReturn(Optional.of(m));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceResponse r = maintenanceService.update(1L, update);

        assertEquals("Updated", r.description());
        verify(vehiclesCache).evict(eq(VehicleFixture.defaultVehicleId()));
    }

    @Test
    void whenDelete_thenRemovesAndEvicts() {
        Maintenance m = VehicleFixture.maintenance();
        when(maintenanceRepository.findById(1L)).thenReturn(Optional.of(m));

        maintenanceService.delete(1L);

        verify(maintenanceRepository).delete(m);
        verify(vehiclesCache).evict(VehicleFixture.defaultVehicleId());
    }
}
