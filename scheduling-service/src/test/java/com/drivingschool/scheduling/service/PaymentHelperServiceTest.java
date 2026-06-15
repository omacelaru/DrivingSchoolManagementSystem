package com.drivingschool.scheduling.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.scheduling.client.PaymentClient;
import com.drivingschool.scheduling.dto.LessonPaymentSyncResponse;
import com.drivingschool.scheduling.dto.PaymentRequest;
import com.drivingschool.scheduling.dto.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentHelperServiceTest {

    @Mock
    private PaymentClient paymentClient;

    private PaymentHelperService paymentHelperService;

    @BeforeEach
    void setUp() {
        paymentHelperService = new PaymentHelperService(paymentClient);
    }

    @Test
    void whenCreatePendingPayment_thenCallsPaymentClient() {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.TEN, 2L, "Test payment");
        PaymentResponse mockResponse = new PaymentResponse(100L, 1L, BigDecimal.TEN, "PENDING", 2L, LocalDateTime.now());
        when(paymentClient.createPendingPayment(request)).thenReturn(ApiResult.success(mockResponse));

        ApiResult<PaymentResponse> result = paymentHelperService.createPendingPayment(request);

        assertNotNull(result);
        assertTrue(result.success());
        assertEquals(mockResponse, result.data());
        verify(paymentClient).createPendingPayment(request);
    }

    @Test
    void whenCreatePendingPaymentFails_thenFallbackReturnsDefault() {
        PaymentRequest request = new PaymentRequest(1L, BigDecimal.TEN, 2L, "Test payment");
        RuntimeException exception = new RuntimeException("Connection timeout");

        ApiResult<PaymentResponse> fallbackResult = paymentHelperService.createPendingPaymentFallback(request, exception);

        assertNotNull(fallbackResult);
        assertTrue(fallbackResult.success());
        PaymentResponse responseData = fallbackResult.data();
        assertNotNull(responseData);
        assertNull(responseData.id());
        assertEquals(BigDecimal.TEN, responseData.amount());
        assertEquals("PENDING_FALLBACK", responseData.status());
    }

    @Test
    void whenReconcilePayments_thenCallsPaymentClient() {
        LessonPaymentSyncResponse mockSyncResponse = new LessonPaymentSyncResponse(2L, 1L, 1, 1);
        when(paymentClient.reconcilePaymentsForCancelledLesson(2L, 1L)).thenReturn(ApiResult.success(mockSyncResponse));

        ApiResult<LessonPaymentSyncResponse> result = paymentHelperService.reconcilePaymentsForCancelledLesson(2L, 1L);

        assertNotNull(result);
        assertTrue(result.success());
        assertEquals(mockSyncResponse, result.data());
        verify(paymentClient).reconcilePaymentsForCancelledLesson(2L, 1L);
    }

    @Test
    void whenReconcilePaymentsFails_thenFallbackReturnsDefault() {
        RuntimeException exception = new RuntimeException("Connection timeout");

        ApiResult<LessonPaymentSyncResponse> fallbackResult = paymentHelperService.reconcilePaymentsFallback(2L, 1L, exception);

        assertNotNull(fallbackResult);
        assertTrue(fallbackResult.success());
        LessonPaymentSyncResponse responseData = fallbackResult.data();
        assertNotNull(responseData);
        assertEquals(2L, responseData.lessonId());
        assertEquals(1L, responseData.studentId());
        assertEquals(0, responseData.cancelledCount());
        assertEquals(0, responseData.refundedCount());
    }
}
