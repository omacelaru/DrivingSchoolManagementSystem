package com.drivingschool.payment.dto;

import com.drivingschool.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request DTO for updating payment status")
public class PaymentStatusUpdateRequest {
    @NotNull(message = "Payment status is required")
    @Schema(description = "New payment status", example = "COMPLETED")
    private Payment.PaymentStatus status;
}

