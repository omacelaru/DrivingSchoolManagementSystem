package com.drivingschool.payment.dto;

import com.drivingschool.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request DTO for processing a payment")
public class PaymentRequest {
    @NotNull(message = "Student ID is required")
    @Schema(description = "ID of the student making the payment", example = "1")
    private Long studentId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Payment amount", example = "500.00")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    @Schema(description = "Payment method used", example = "CARD")
    private Payment.PaymentMethod paymentMethod;

    @Schema(description = "Transaction ID from payment gateway (optional)", example = "TXN-123456")
    private String transactionId;

    @Schema(description = "Additional notes about the payment", example = "Payment for beginner course")
    private String notes;

    @Schema(description = "ID of the course being paid for (optional)", example = "1")
    private Long courseId;
}

