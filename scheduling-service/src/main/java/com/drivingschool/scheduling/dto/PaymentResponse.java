package com.drivingschool.scheduling.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
    Long id,
    Long studentId,
    BigDecimal amount,
    String status,
    Long lessonId,
    LocalDateTime createdAt
) {
}

