package com.drivingschool.scheduling.controller;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.scheduling.dto.InstructorResponse;
import com.drivingschool.scheduling.dto.PaymentRequest;
import com.drivingschool.scheduling.dto.PaymentResponse;
import com.drivingschool.scheduling.service.InstructorHelperService;
import com.drivingschool.scheduling.service.PaymentHelperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/resilience-demo")
@RequiredArgsConstructor
@Tag(name = "Resilience Demo", description = "Endpoints for demonstrating Resilience4j Circuit Breakers, Retries, and Fallbacks")
public class ResilienceDemoController {

    private final PaymentHelperService paymentHelperService;
    private final InstructorHelperService instructorHelperService;

    @PostMapping("/payment/simulate-failure")
    @Operation(summary = "Enable/disable simulated failures for payment-service calls")
    public ResponseEntity<ApiResult<String>> togglePaymentFailure(@RequestParam boolean enabled) {
        paymentHelperService.setSimulateFailure(enabled);
        String state = enabled ? "ENABLED" : "DISABLED";
        return ResponseEntity.ok(ApiResult.success("Simulated payment failure is now " + state, state));
    }

    @GetMapping("/payment/simulate-failure")
    @Operation(summary = "Get current state of simulated payment failures")
    public ResponseEntity<ApiResult<Boolean>> getPaymentFailureState() {
        return ResponseEntity.ok(ApiResult.success(paymentHelperService.isSimulateFailure()));
    }

    @PostMapping("/payment/test")
    @Operation(summary = "Execute a test call to payment-service to observe retries and circuit breaker")
    public ResponseEntity<ApiResult<PaymentResponse>> testPaymentCall() {
        PaymentRequest request = new PaymentRequest(
                999L,
                BigDecimal.valueOf(150.0),
                888L,
                "Resilience Test Payment Request"
        );
        ApiResult<PaymentResponse> result = paymentHelperService.createPendingPayment(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/instructor/simulate-failure")
    @Operation(summary = "Enable/disable simulated failures for instructor-service calls")
    public ResponseEntity<ApiResult<String>> toggleInstructorFailure(@RequestParam boolean enabled) {
        instructorHelperService.setSimulateFailure(enabled);
        String state = enabled ? "ENABLED" : "DISABLED";
        return ResponseEntity.ok(ApiResult.success("Simulated instructor failure is now " + state, state));
    }

    @GetMapping("/instructor/simulate-failure")
    @Operation(summary = "Get current state of simulated instructor failures")
    public ResponseEntity<ApiResult<Boolean>> getInstructorFailureState() {
        return ResponseEntity.ok(ApiResult.success(instructorHelperService.isSimulateFailure()));
    }

    @GetMapping("/instructor/test/{id}")
    @Operation(summary = "Execute a test call to instructor-service to observe retries and circuit breaker")
    public ResponseEntity<ApiResult<InstructorResponse>> testInstructorCall(@PathVariable Long id) {
        try {
            InstructorResponse response = instructorHelperService.getInstructorOrThrow(id);
            return ResponseEntity.ok(ApiResult.success("Call succeeded", response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResult.success("Call failed (handled): " + e.getMessage(), null));
        }
    }
}
