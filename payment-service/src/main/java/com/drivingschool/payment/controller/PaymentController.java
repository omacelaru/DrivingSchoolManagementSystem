package com.drivingschool.payment.controller;

import com.drivingschool.common.dto.ApiResponse;
import com.drivingschool.payment.dto.PaymentRequest;
import com.drivingschool.payment.dto.PaymentResponse;
import com.drivingschool.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for processing payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Process a payment", description = "Processes a new payment for a student")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment processed successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves payment details")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @Parameter(description = "Payment ID") @PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get student payments", description = "Retrieves all payments for a student")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getStudentPayments(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        List<PaymentResponse> payments = paymentService.getStudentPayments(studentId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/student/{studentId}/balance")
    @Operation(summary = "Get student balance", description = "Calculates total balance for a student")
    public ResponseEntity<ApiResponse<BigDecimal>> getStudentBalance(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        BigDecimal balance = paymentService.getStudentBalance(studentId);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }
}

