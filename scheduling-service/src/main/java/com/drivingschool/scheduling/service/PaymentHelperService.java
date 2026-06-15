package com.drivingschool.scheduling.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.scheduling.client.PaymentClient;
import com.drivingschool.scheduling.dto.LessonPaymentSyncResponse;
import com.drivingschool.scheduling.dto.PaymentRequest;
import com.drivingschool.scheduling.dto.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentHelperService {

    private final PaymentClient paymentClient;
    @Getter
    @Setter
    private boolean simulateFailure = false;

    @CircuitBreaker(name = "paymentService")
    @Retry(name = "paymentService", fallbackMethod = "createPendingPaymentFallback")
    public ApiResult<PaymentResponse> createPendingPayment(PaymentRequest request) {
        if (simulateFailure) {
            log.warn("Simulating payment-service - createPendingPayment failure (as requested)...");
            throw new RuntimeException("Simulated payment-service failure");
        }
        log.info("Sending payment request to payment-service for lesson ID: {}", request.lessonId());
        return paymentClient.createPendingPayment(request);
    }

    @CircuitBreaker(name = "paymentService")
    @Retry(name = "paymentService", fallbackMethod = "reconcilePaymentsFallback")
    public ApiResult<LessonPaymentSyncResponse> reconcilePaymentsForCancelledLesson(Long lessonId, Long studentId) {
        log.info("Sending reconciliation request to payment-service for cancelled lesson ID: {}", lessonId);
        return paymentClient.reconcilePaymentsForCancelledLesson(lessonId, studentId);
    }

    // Fallback method for createPendingPayment
    public ApiResult<PaymentResponse> createPendingPaymentFallback(PaymentRequest request, Throwable t) {
        log.error("Fallback invoked for createPendingPayment. payment-service failed: {}", t.getMessage());
        PaymentResponse fallbackResponse = new PaymentResponse(
                null,
                request.studentId(),
                request.amount(),
                "PENDING_FALLBACK",
                request.lessonId(),
                null
        );
        return ApiResult.success("Payment deferred due to payment-service outage (Fallback)", fallbackResponse);
    }

    // Fallback method for reconcilePaymentsForCancelledLesson
    public ApiResult<LessonPaymentSyncResponse> reconcilePaymentsFallback(Long lessonId, Long studentId, Throwable t) {
        log.error("Fallback invoked for reconcilePaymentsForCancelledLesson. payment-service failed: {}", t.getMessage());
        LessonPaymentSyncResponse fallbackResponse = new LessonPaymentSyncResponse(lessonId, studentId, 0, 0);
        return ApiResult.success("Reconciliation deferred due to payment-service outage (Fallback)", fallbackResponse);
    }
}
