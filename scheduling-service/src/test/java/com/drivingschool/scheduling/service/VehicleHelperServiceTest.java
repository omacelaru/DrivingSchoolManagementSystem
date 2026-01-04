package com.drivingschool.scheduling.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.scheduling.client.VehicleClient;
import com.drivingschool.scheduling.dto.VehicleResponse;
import com.drivingschool.scheduling.fixture.VehicleResponseFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleHelperServiceTest {

    @Mock
    private VehicleClient vehicleClient;

    @InjectMocks
    private VehicleHelperService vehicleHelperService;

    private VehicleResponse vehicleResponse;

    @BeforeEach
    void setUp() {
        vehicleResponse = VehicleResponseFixture.vehicleResponseAvailable();
    }

    @Test
    void testGetVehicleOrThrow_Success() {
        // Given
        ApiResult<VehicleResponse> apiResult = ApiResult.success(vehicleResponse);
        when(vehicleClient.getVehicleById(1L)).thenReturn(apiResult);

        // When
        VehicleResponse result = vehicleHelperService.getVehicleOrThrow(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("AB-12-CDE", result.licensePlate());
    }

    @Test
    void testGetVehicleOrThrow_NotFound() {
        // Given
        when(vehicleClient.getVehicleById(1L)).thenReturn(null);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> vehicleHelperService.getVehicleOrThrow(1L));
    }

    @Test
    void testGetVehicleOrThrow_NullData() {
        // Given
        ApiResult<VehicleResponse> apiResult = ApiResult.success(null);
        when(vehicleClient.getVehicleById(1L)).thenReturn(apiResult);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> vehicleHelperService.getVehicleOrThrow(1L));
    }

    @Test
    void testValidateVehicleForUse_Success() {
        // Given
        ApiResult<VehicleResponse> apiResult = ApiResult.success(vehicleResponse);
        when(vehicleClient.getVehicleById(1L)).thenReturn(apiResult);

        // When
        assertDoesNotThrow(() -> vehicleHelperService.validateVehicleForUse(1L));

        // Then - No exception should be thrown
    }

    @Test
    void testValidateVehicleForUse_NotFound() {
        // Given
        when(vehicleClient.getVehicleById(1L)).thenReturn(null);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> vehicleHelperService.validateVehicleForUse(1L));
    }

    @Test
    void testValidateVehicleForUse_NotAvailable() {
        // Given
        VehicleResponse unavailableVehicle = VehicleResponseFixture.vehicleResponseMaintenance();

        ApiResult<VehicleResponse> apiResult = ApiResult.success(unavailableVehicle);
        when(vehicleClient.getVehicleById(VehicleResponseFixture.defaultVehicleId())).thenReturn(apiResult);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> vehicleHelperService.validateVehicleForUse(1L));

        assertEquals("VEHICLE_NOT_AVAILABLE", exception.getErrorCode());
    }

    @Test
    void testValidateVehicleForUse_InsuranceExpired() {
        // Given
        VehicleResponse expiredInsuranceVehicle = VehicleResponseFixture.vehicleResponseWithExpiredInsurance();

        ApiResult<VehicleResponse> apiResult = ApiResult.success(expiredInsuranceVehicle);
        when(vehicleClient.getVehicleById(VehicleResponseFixture.defaultVehicleId())).thenReturn(apiResult);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> vehicleHelperService.validateVehicleForUse(1L));

        assertEquals("VEHICLE_INSURANCE_EXPIRED", exception.getErrorCode());
    }

    @Test
    void testValidateVehicleForUse_InsuranceExpiresToday() {
        // Given
        VehicleResponse todayExpiryVehicle = new VehicleResponse(1L, "AB-12-CDE", "Toyota", "Corolla", 2020, LocalDate.now().minusDays(1), // Expires yesterday
                "AVAILABLE");

        ApiResult<VehicleResponse> apiResult = ApiResult.success(todayExpiryVehicle);
        when(vehicleClient.getVehicleById(1L)).thenReturn(apiResult);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> vehicleHelperService.validateVehicleForUse(1L));

        assertEquals("VEHICLE_INSURANCE_EXPIRED", exception.getErrorCode());
    }

    @Test
    void testValidateVehicleForUse_InsuranceNull() {
        // Given
        VehicleResponse nullInsuranceVehicle = new VehicleResponse(1L, "AB-12-CDE", "Toyota", "Corolla", 2020, null, // No insurance expiry
                "AVAILABLE");

        ApiResult<VehicleResponse> apiResult = ApiResult.success(nullInsuranceVehicle);
        when(vehicleClient.getVehicleById(1L)).thenReturn(apiResult);

        // When - Should not throw exception if insurance is null
        assertDoesNotThrow(() -> vehicleHelperService.validateVehicleForUse(1L));
    }
}

