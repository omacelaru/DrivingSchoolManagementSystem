package com.drivingschool.payment.controller;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.payment.dto.PaymentPendingRequest;
import com.drivingschool.payment.dto.PaymentRequest;
import com.drivingschool.payment.dto.PaymentResponse;
import com.drivingschool.payment.dto.PaymentStatusUpdateRequest;
import com.drivingschool.payment.entity.Payment;
import com.drivingschool.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Payment Management",
        description = "Payments: process, refund, status updates, and student history. "
                + "DELETE removes only PENDING records (cancel before completion); COMPLETED/REFUNDED/FAILED/CANCELLED cannot be deleted.")
public class PaymentController {
    private final PaymentService paymentService;

    @PutMapping
    @Operation(summary = "Process a payment",
              description = "Processes a payment for a student. If a PENDING payment exists for the specified lessonId (or matching studentId and amount), it will be updated to COMPLETED. Otherwise, a new payment will be created. Validates payment amount and method.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment processed successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation failed"),
        @ApiResponse(responseCode = "404", description = "Student or course not found"),
        @ApiResponse(responseCode = "500", description = "Payment processing failed")
    })
    public ResponseEntity<ApiResult<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("Payment processed successfully", response));
    }

    @PostMapping("/pending")
    @Operation(summary = "Create a pending payment",
              description = "Creates a pending payment for a lesson that requires payment. Used when booking additional lessons outside of a course. Requires studentId and amount.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pending payment created successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation failed")
    })
    public ResponseEntity<ApiResult<PaymentResponse>> createPendingPayment(
            @Valid @RequestBody PaymentPendingRequest request) {
        PaymentResponse response = paymentService.createPendingPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("Pending payment created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID",
              description = "Retrieves detailed information about a specific payment, including status, amount, and payment method.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment found",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<ApiResult<PaymentResponse>> getPayment(
            @Parameter(description = "Unique payment identifier", example = "1", required = true)
            @PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete pending payment (cancel)",
              description = "Hard-deletes the payment row when status is PENDING (e.g. lesson cancelled before pay). "
                      + "COMPLETED and REFUNDED payments are never deleted. FAILED and CANCELLED are also rejected.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pending payment removed"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "409", description = "Payment is not PENDING (e.g. already completed)")
    })
    public ResponseEntity<ApiResult<Void>> deletePendingPayment(
            @Parameter(description = "Unique payment identifier", example = "1", required = true)
            @PathVariable Long id) {
        paymentService.deletePendingPayment(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResult.success("Pending payment deleted", null));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get student payments",
              description = "Retrieves all payment transactions for a specific student, ordered by transaction date (newest first). Optionally filter by payment status.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<ApiResult<List<PaymentResponse>>> getStudentPayments(
            @PathVariable Long studentId,
            @Parameter(description = "Filter by payment status (optional)", example = "PENDING")
            @RequestParam(required = false) Payment.PaymentStatus status) {
        List<PaymentResponse> payments = paymentService.getStudentPayments(studentId, status);
        return ResponseEntity.ok(ApiResult.success(payments));
    }

    @GetMapping("/student/{studentId}/balance")
    @Operation(summary = "Get student balance",
              description = "Calculates the total balance (sum of all payments) for a student. Returns the total amount paid.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance calculated successfully",
                    content = @Content(schema = @Schema(implementation = BigDecimal.class))),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<ApiResult<BigDecimal>> getStudentBalance(
            @Parameter(description = "Unique student identifier", example = "1", required = true)
            @PathVariable Long studentId) {
        BigDecimal balance = paymentService.getStudentBalance(studentId);
        return ResponseEntity.ok(ApiResult.success(balance));
    }

    @PutMapping("/{id}/refund")
    @Operation(summary = "Refund a payment",
              description = "Processes a refund for a completed payment. Uses pessimistic locking to prevent concurrent refunds. Only completed payments can be refunded.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment refunded successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Payment cannot be refunded (invalid status or already refunded)"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<ApiResult<PaymentResponse>> refundPayment(
            @Parameter(description = "Unique payment identifier", example = "1", required = true)
            @PathVariable Long id) {
        PaymentResponse response = paymentService.refundPayment(id);
        return ResponseEntity.ok(ApiResult.success("Payment refunded successfully", response));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update payment status",
              description = "Updates the status of a payment. Uses pessimistic locking to prevent concurrent status updates. Refunded payments cannot have their status changed.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment status updated successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status change (e.g., trying to change refunded payment)"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<ApiResult<PaymentResponse>> updatePaymentStatus(
            @Parameter(description = "Unique payment identifier", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PaymentStatusUpdateRequest request) {
        PaymentResponse response = paymentService.updatePaymentStatus(id, request.status());
        return ResponseEntity.ok(ApiResult.success("Payment status updated successfully", response));
    }
}

