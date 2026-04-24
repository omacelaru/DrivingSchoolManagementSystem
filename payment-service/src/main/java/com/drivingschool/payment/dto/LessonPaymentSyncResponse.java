package com.drivingschool.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary of payment actions applied after lesson lifecycle changes.")
public record LessonPaymentSyncResponse(
        @Schema(description = "Lesson identifier used for reconciliation", example = "42")
        Long lessonId,
        @Schema(description = "Student identifier used for reconciliation", example = "7")
        Long studentId,
        @Schema(description = "Number of pending payments moved to CANCELLED", example = "1")
        int cancelledCount,
        @Schema(description = "Number of completed payments moved to REFUNDED", example = "0")
        int refundedCount
) {
}
