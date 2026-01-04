package com.drivingschool.payment.dto;

import com.drivingschool.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO for processing a payment")
public record PaymentRequest(
    @NotNull(message = "Student ID is required")
    @Positive(message = "Student ID must be positive")
    @Schema(description = "ID of the student making the payment", example = "1")
    Long studentId,

    @NotNull(message = "Payment method is required when processing payment")
    @Schema(description = "Payment method used (optional for pending payments)", example = "CARD")
    Payment.PaymentMethod paymentMethod,

    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9]{2,20}(-[A-Za-z0-9]{1,20})*$", message = "Transaction ID must be alphanumeric with optional hyphens (e.g., TXN-123456, txn123456)")
    @Schema(description = "Transaction ID from payment gateway (optional)", example = "TXN-123456")
    String transactionId,

    @NotNull(message = "Lesson ID is required")
    @Positive(message = "Lesson ID must be positive")
    @Schema(description = "ID of the lesson being paid", example = "1")
    Long lessonId
) {
}

