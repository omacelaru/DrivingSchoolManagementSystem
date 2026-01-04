package com.drivingschool.vehicle.fixture;

import com.drivingschool.vehicle.dto.VehicleRequest;
import com.drivingschool.vehicle.dto.VehicleResponse;
import com.drivingschool.vehicle.entity.Maintenance;
import com.drivingschool.vehicle.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class VehicleFixture {

    public static final VehicleFixture INSTANCE = VehicleFixture.builder()
            .defaultVehicleId(1L)
            .defaultLicensePlate("AB-12-CDE")
            .defaultMake("Toyota")
            .defaultModel("Corolla")
            .defaultYear(2020)
            .defaultInsuranceExpiry(LocalDate.now().plusYears(1))
            .build();

    private final Long defaultVehicleId;
    private final String defaultLicensePlate;
    private final String defaultMake;
    private final String defaultModel;
    private final Integer defaultYear;
    private final LocalDate defaultInsuranceExpiry;

    public static Long defaultVehicleId() {
        return INSTANCE.getDefaultVehicleId();
    }

    public static String defaultLicensePlate() {
        return INSTANCE.getDefaultLicensePlate();
    }

    public static String defaultMake() {
        return INSTANCE.getDefaultMake();
    }

    public static String defaultModel() {
        return INSTANCE.getDefaultModel();
    }

    public static Integer defaultYear() {
        return INSTANCE.getDefaultYear();
    }

    public static LocalDate defaultInsuranceExpiry() {
        return INSTANCE.getDefaultInsuranceExpiry();
    }

    public static VehicleRequest vehicleRequest() {
        return new VehicleRequest(
                defaultLicensePlate(),
                defaultMake(),
                defaultModel(),
                defaultYear(),
                defaultInsuranceExpiry()
        );
    }

    public static VehicleRequest vehicleRequest(String licensePlate) {
        return new VehicleRequest(
                licensePlate,
                defaultMake(),
                defaultModel(),
                defaultYear(),
                defaultInsuranceExpiry()
        );
    }

    public static Vehicle vehicle() {
        return Vehicle.builder()
                .id(defaultVehicleId())
                .licensePlate(defaultLicensePlate())
                .make(defaultMake())
                .model(defaultModel())
                .year(defaultYear())
                .insuranceExpiry(defaultInsuranceExpiry())
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Vehicle vehicle(Long id, Vehicle.VehicleStatus status) {
        return Vehicle.builder()
                .id(id)
                .licensePlate(defaultLicensePlate())
                .make(defaultMake())
                .model(defaultModel())
                .year(defaultYear())
                .insuranceExpiry(defaultInsuranceExpiry())
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Vehicle vehicleAvailable() {
        return vehicle(defaultVehicleId(), Vehicle.VehicleStatus.AVAILABLE);
    }

    public static Vehicle vehicleInMaintenance() {
        return vehicle(defaultVehicleId(), Vehicle.VehicleStatus.MAINTENANCE);
    }

    public static Vehicle vehicleWithExpiredInsurance() {
        return Vehicle.builder()
                .id(defaultVehicleId())
                .licensePlate(defaultLicensePlate())
                .make(defaultMake())
                .model(defaultModel())
                .year(defaultYear())
                .insuranceExpiry(LocalDate.now().minusDays(1))
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static VehicleResponse vehicleResponse() {
        return new VehicleResponse(
                defaultVehicleId(),
                defaultLicensePlate(),
                defaultMake(),
                defaultModel(),
                defaultYear(),
                defaultInsuranceExpiry(),
                Vehicle.VehicleStatus.AVAILABLE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static VehicleResponse vehicleResponse(Long id, Vehicle.VehicleStatus status) {
        return new VehicleResponse(
                id,
                defaultLicensePlate(),
                defaultMake(),
                defaultModel(),
                defaultYear(),
                defaultInsuranceExpiry(),
                status,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static Maintenance maintenance() {
        return Maintenance.builder()
                .id(1L)
                .vehicle(vehicle())
                .maintenanceDate(LocalDate.now())
                .cost(0.0)
                .type(Maintenance.MaintenanceType.OTHER)
                .description("Vehicle sent to maintenance")
                .build();
    }
}
