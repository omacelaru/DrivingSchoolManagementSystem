package com.drivingschool.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request DTO for creating a pending payment")
public record PaymentPendingRequest(
    @NotNull(message = "Student ID is required")
    @Positive(message = "Student ID must be positive")
    @Schema(description = "ID of the student making the payment", example = "1")
    Long studentId,

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Payment amount", example = "500.00")
    BigDecimal amount,

    @Positive(message = "Lesson ID must be positive")
    @Schema(description = "ID of the lesson being paid for (optional)", example = "1")
    Long lessonId,

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Schema(description = "Additional notes about the payment", example = "Payment for additional lesson")
    String notes
) {
}

