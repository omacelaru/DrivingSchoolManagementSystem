package com.drivingschool.scheduling.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long studentId;
    private BigDecimal amount;
    private String status;
    private Long lessonId;
    private LocalDateTime createdAt;
}

