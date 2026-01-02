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

    @Schema(description = "Payment method used (optional for pending payments)", example = "CARD")
    @NotNull(message = "Payment method is required when processing payment")
    private Payment.PaymentMethod paymentMethod;

    @Schema(description = "Transaction ID from payment gateway (optional)", example = "TXN-123456")
    private String transactionId;

    @Schema(description = "ID of the lesson being paid)", example = "1")
    private Long lessonId;
}

