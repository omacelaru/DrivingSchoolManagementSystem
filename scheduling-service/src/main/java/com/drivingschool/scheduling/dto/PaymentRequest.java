package com.drivingschool.scheduling.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private Long studentId;
    private BigDecimal amount;
    private Long lessonId;
    private String notes;
}

