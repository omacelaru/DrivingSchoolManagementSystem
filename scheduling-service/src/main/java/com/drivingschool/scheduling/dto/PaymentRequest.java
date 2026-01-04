package com.drivingschool.scheduling.dto;

import java.math.BigDecimal;

public record PaymentRequest(
    Long studentId,
    BigDecimal amount,
    Long lessonId,
    String notes
) {
}

