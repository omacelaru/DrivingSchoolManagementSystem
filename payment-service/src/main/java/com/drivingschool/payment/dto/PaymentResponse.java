package com.drivingschool.payment.dto;

import com.drivingschool.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Response DTO containing payment information")
public record PaymentResponse(
    @Schema(description = "Unique payment identifier", example = "1")
    Long id,
    
    @Schema(description = "ID of the student who made the payment", example = "1")
    Long studentId,
    
    @Schema(description = "Payment amount", example = "500.00")
    BigDecimal amount,
    
    @Schema(description = "Payment method used", example = "CARD")
    Payment.PaymentMethod paymentMethod,
    
    @Schema(description = "Payment status", example = "COMPLETED")
    Payment.PaymentStatus status,
    
    @Schema(description = "Date and time of the transaction", example = "2027-01-01T10:30:00")
    LocalDateTime transactionDate,
    
    @Schema(description = "Transaction ID from payment gateway", example = "TXN-123456")
    String transactionId,
    
    @Schema(description = "ID of the lesson being paid for (for additional lessons)", example = "1")
    Long lessonId,
    
    @Schema(description = "Additional notes about the payment", example = "Payment for beginner course")
    String notes,
    
    @Schema(description = "Date and time when payment was created", example = "2027-01-01T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "Date and time when payment was last modified", example = "2027-01-01T10:30:00")
    LocalDateTime lastModifiedDate
) {
}

