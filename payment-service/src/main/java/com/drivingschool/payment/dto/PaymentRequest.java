package com.drivingschool.payment.dto;

import com.drivingschool.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request DTO for processing a payment")
public record PaymentRequest(
    @NotNull(message = "Student ID is required")
    @Schema(description = "ID of the student making the payment", example = "1")
    Long studentId,

    @Schema(description = "Payment method used (optional for pending payments)", example = "CARD")
    @NotNull(message = "Payment method is required when processing payment")
    Payment.PaymentMethod paymentMethod,

    @Schema(description = "Transaction ID from payment gateway (optional)", example = "TXN-123456")
    String transactionId,

    @Schema(description = "ID of the lesson being paid)", example = "1")
    @NotNull(message = "Lesson ID is required")
    Long lessonId
) {
}

