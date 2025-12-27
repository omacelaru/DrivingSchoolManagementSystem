package com.drivingschool.payment.dto;

import com.drivingschool.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing payment information")
public class PaymentResponse {
    @Schema(description = "Unique payment identifier", example = "1")
    private Long id;
    
    @Schema(description = "ID of the student who made the payment", example = "1")
    private Long studentId;
    
    @Schema(description = "Payment amount", example = "500.00")
    private BigDecimal amount;
    
    @Schema(description = "Payment method used", example = "CARD")
    private Payment.PaymentMethod paymentMethod;
    
    @Schema(description = "Payment status", example = "COMPLETED")
    private Payment.PaymentStatus status;
    
    @Schema(description = "Date and time of the transaction", example = "2024-01-15T10:30:00")
    private LocalDateTime transactionDate;
    
    @Schema(description = "ID of the generated invoice", example = "1")
    private Long invoiceId;
    
    @Schema(description = "ID of the course being paid for", example = "1")
    private Long courseId;
    
    @Schema(description = "Date and time when payment was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}

