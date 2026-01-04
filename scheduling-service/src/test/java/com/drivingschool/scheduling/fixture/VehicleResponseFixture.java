package com.drivingschool.scheduling.fixture;

import com.drivingschool.scheduling.dto.VehicleResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class VehicleResponseFixture {

    public static final VehicleResponseFixture INSTANCE = VehicleResponseFixture.builder()
            .defaultVehicleId(1L)
            .defaultLicensePlate("AB-12-CDE")
            .defaultMake("Toyota")
            .defaultModel("Corolla")
            .defaultYear(2020)
            .defaultInsuranceExpiry(LocalDate.now().plusYears(1))
            .defaultStatus("AVAILABLE")
            .build();

    private final Long defaultVehicleId;
    private final String defaultLicensePlate;
    private final String defaultMake;
    private final String defaultModel;
    private final Integer defaultYear;
    private final LocalDate defaultInsuranceExpiry;
    private final String defaultStatus;

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

    public static String defaultStatus() {
        return INSTANCE.getDefaultStatus();
    }

    public static VehicleResponse vehicleResponse() {
        return new VehicleResponse(
                defaultVehicleId(),
                defaultLicensePlate(),
                defaultMake(),
                defaultModel(),
                defaultYear(),
                defaultInsuranceExpiry(),
                defaultStatus()
        );
    }

    public static VehicleResponse vehicleResponse(String status) {
        return new VehicleResponse(
                defaultVehicleId(),
                defaultLicensePlate(),
                defaultMake(),
                defaultModel(),
                defaultYear(),
                defaultInsuranceExpiry(),
                status
        );
    }

    public static VehicleResponse vehicleResponseAvailable() {
        return vehicleResponse("AVAILABLE");
    }

    public static VehicleResponse vehicleResponseMaintenance() {
        return vehicleResponse("MAINTENANCE");
    }

    public static VehicleResponse vehicleResponseWithExpiredInsurance() {
        return new VehicleResponse(
                defaultVehicleId(),
                defaultLicensePlate(),
                defaultMake(),
                defaultModel(),
                defaultYear(),
                LocalDate.now().minusDays(1),
                "AVAILABLE"
        );
    }
}
