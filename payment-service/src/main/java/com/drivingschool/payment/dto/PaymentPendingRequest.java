package com.drivingschool.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request DTO for creating a pending payment")
public class PaymentPendingRequest {
    @NotNull(message = "Student ID is required")
    @Schema(description = "ID of the student making the payment", example = "1")
    private Long studentId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Payment amount", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "ID of the lesson being paid for (optional)", example = "1")
    private Long lessonId;

    @Schema(description = "Additional notes about the payment", example = "Payment for additional lesson")
    private String notes;
}

