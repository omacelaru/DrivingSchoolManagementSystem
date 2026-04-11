package com.drivingschool.payment.fixture;

import com.drivingschool.payment.dto.PaymentPendingRequest;
import com.drivingschool.payment.dto.PaymentRequest;
import com.drivingschool.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PaymentFixture {

    public static final PaymentFixture INSTANCE = PaymentFixture.builder()
            .defaultPaymentMethod(Payment.PaymentMethod.CARD)
            .defaultStatus(Payment.PaymentStatus.PENDING)
            .defaultAmount(new BigDecimal("500.00"))
            .defaultStudentId(1L)
            .defaultLessonId(1L)
            .defaultTransactionId("TXN-123456")
            .build();

    private final Payment.PaymentMethod defaultPaymentMethod;
    private final Payment.PaymentStatus defaultStatus;
    private final BigDecimal defaultAmount;
    private final Long defaultStudentId;
    private final Long defaultLessonId;
    private final String defaultTransactionId;

    public static Payment.PaymentMethod defaultPaymentMethod() {
        return INSTANCE.getDefaultPaymentMethod();
    }

    public static BigDecimal defaultAmount() {
        return INSTANCE.getDefaultAmount();
    }

    public static Long defaultStudentId() {
        return INSTANCE.getDefaultStudentId();
    }

    public static Long defaultLessonId() {
        return INSTANCE.getDefaultLessonId();
    }

    public static String defaultTransactionId() {
        return INSTANCE.getDefaultTransactionId();
    }

    public static PaymentRequest paymentRequest() {
        return new PaymentRequest(
                defaultStudentId(),
                defaultPaymentMethod(),
                defaultTransactionId(),
                defaultLessonId()
        );
    }

    public static PaymentRequest paymentRequestWithoutTransactionId() {
        return new PaymentRequest(
                defaultStudentId(),
                defaultPaymentMethod(),
                null,
                defaultLessonId()
        );
    }

    public static PaymentPendingRequest paymentPendingRequest() {
        return new PaymentPendingRequest(
                defaultStudentId(),
                defaultAmount(),
                defaultLessonId(),
                "Test notes"
        );
    }

    public static Payment payment(Long id, Payment.PaymentStatus status) {
        return Payment.builder()
                .id(id)
                .studentId(defaultStudentId())
                .amount(defaultAmount())
                .paymentMethod(defaultPaymentMethod())
                .status(status)
                .transactionId(defaultTransactionId())
                .lessonId(defaultLessonId())
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();
    }

    public static Payment paymentCompleted() {
        return payment(1L, Payment.PaymentStatus.COMPLETED);
    }

    public static Payment paymentRefunded() {
        return payment(1L, Payment.PaymentStatus.REFUNDED);
    }

    public static Payment paymentPending() {
        return payment(1L, Payment.PaymentStatus.PENDING);
    }

    public static Payment paymentFailed() {
        return payment(1L, Payment.PaymentStatus.FAILED);
    }

    public static Payment paymentCancelled() {
        return payment(1L, Payment.PaymentStatus.CANCELLED);
    }
}

