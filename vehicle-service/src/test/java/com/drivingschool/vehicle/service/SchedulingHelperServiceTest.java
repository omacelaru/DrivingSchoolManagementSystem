package com.drivingschool.vehicle.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.vehicle.client.SchedulingClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingHelperServiceTest {

    @Mock
    private SchedulingClient schedulingClient;

    private SchedulingHelperService schedulingHelperService;

    @BeforeEach
    void setUp() {
        schedulingHelperService = new SchedulingHelperService(schedulingClient);
    }

    @Test
    void whenFetchVehicleCourseAssignmentExists_thenCallsSchedulingClient() {
        when(schedulingClient.fetchVehicleCourseAssignmentExists(1L)).thenReturn(ApiResult.success(true));

        ApiResult<Boolean> result = schedulingHelperService.fetchVehicleCourseAssignmentExists(1L);

        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.data());
        verify(schedulingClient).fetchVehicleCourseAssignmentExists(1L);
    }

    @Test
    void whenFetchVehicleCourseAssignmentExistsFails_thenFallbackReturnsTrue() {
        RuntimeException exception = new RuntimeException("Scheduling down");

        ApiResult<Boolean> fallbackResult = schedulingHelperService.fetchVehicleCourseAssignmentExistsFallback(1L, exception);

        assertNotNull(fallbackResult);
        assertTrue(fallbackResult.success());
        assertTrue(fallbackResult.data()); // Fallback blocks deletion by returning true
    }

    @Test
    void whenIsVehicleAvailable_thenCallsSchedulingClient() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        when(schedulingClient.isVehicleAvailable(1L, start, end)).thenReturn(true);

        Boolean result = schedulingHelperService.isVehicleAvailable(1L, start, end);

        assertNotNull(result);
        assertTrue(result);
        verify(schedulingClient).isVehicleAvailable(1L, start, end);
    }

    @Test
    void whenIsVehicleAvailableFails_thenFallbackReturnsTrue() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        RuntimeException exception = new RuntimeException("Scheduling down");

        Boolean fallbackResult = schedulingHelperService.isVehicleAvailableFallback(1L, start, end, exception);

        assertNotNull(fallbackResult);
        assertTrue(fallbackResult); // Fallback treats vehicle as available
    }
}
