package com.drivingschool.scheduling.dto;

public record LessonPaymentSyncResponse(
        Long lessonId,
        Long studentId,
        int cancelledCount,
        int refundedCount
) {
}
