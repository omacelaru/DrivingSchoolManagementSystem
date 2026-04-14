package com.drivingschool.payment.integration;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.payment.entity.Payment;
import com.drivingschool.payment.repository.PaymentRepository;
import com.drivingschool.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("local-h2")
@Transactional
class PaymentDeleteIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void deletePendingPayment_deletesPendingAndRejectsCompleted() {
        Payment pending = paymentRepository.save(Payment.builder()
                .studentId(1L)
                .lessonId(100L)
                .amount(new BigDecimal("250.00"))
                .status(Payment.PaymentStatus.PENDING)
                .build());

        paymentService.deletePendingPayment(pending.getId());
        assertThat(paymentRepository.findById(pending.getId())).isEmpty();

        Payment completed = paymentRepository.save(Payment.builder()
                .studentId(2L)
                .lessonId(101L)
                .amount(new BigDecimal("300.00"))
                .status(Payment.PaymentStatus.COMPLETED)
                .paymentMethod(Payment.PaymentMethod.CARD)
                .transactionId("TXN-INTEGRATION-1")
                .build());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> paymentService.deletePendingPayment(completed.getId())
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_DELETE_NOT_ALLOWED.getCode());
        assertThat(paymentRepository.findById(completed.getId())).isPresent();
    }
}

