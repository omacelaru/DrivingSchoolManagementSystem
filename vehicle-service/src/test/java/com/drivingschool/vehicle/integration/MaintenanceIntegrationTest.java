package com.drivingschool.vehicle.integration;

import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.vehicle.dto.MaintenanceRequest;
import com.drivingschool.vehicle.dto.MaintenanceResponse;
import com.drivingschool.vehicle.entity.Maintenance;
import com.drivingschool.vehicle.entity.Vehicle;
import com.drivingschool.vehicle.repository.MaintenanceRepository;
import com.drivingschool.vehicle.repository.VehicleRepository;
import com.drivingschool.vehicle.service.MaintenanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("local-h2")
@Transactional
class MaintenanceIntegrationTest {

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Test
    void createUpdateDeleteMaintenance_persistsAndRemovesRecord() {
        Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                .licensePlate("B-99-INT")
                .make("Dacia")
                .model("Logan")
                .year(2022)
                .insuranceExpiry(LocalDate.now().plusMonths(6))
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .build());

        MaintenanceResponse created = maintenanceService.createForVehicle(
                vehicle.getId(),
                new MaintenanceRequest(
                        LocalDate.now(),
                        "Initial inspection",
                        120.0,
                        Maintenance.MaintenanceType.INSPECTION
                )
        );
        assertThat(created.id()).isNotNull();
        assertThat(maintenanceRepository.findById(created.id())).isPresent();

        MaintenanceResponse updated = maintenanceService.update(
                created.id(),
                new MaintenanceRequest(
                        LocalDate.now().plusDays(1),
                        "Updated maintenance notes",
                        180.0,
                        Maintenance.MaintenanceType.REPAIR
                )
        );
        assertThat(updated.cost()).isEqualTo(180.0);
        assertThat(updated.type()).isEqualTo(Maintenance.MaintenanceType.REPAIR);

        maintenanceService.delete(created.id());
        assertThat(maintenanceRepository.findById(created.id())).isEmpty();
        assertThrows(ResourceNotFoundException.class, () -> maintenanceService.getById(created.id()));
    }
}

