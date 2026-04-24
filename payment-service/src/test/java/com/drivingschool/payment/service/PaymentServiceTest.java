package com.drivingschool.payment.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.payment.dto.PaymentPendingRequest;
import com.drivingschool.payment.dto.PaymentRequest;
import com.drivingschool.payment.dto.PaymentResponse;
import com.drivingschool.payment.entity.Payment;
import com.drivingschool.payment.fixture.PaymentFixture;
import com.drivingschool.payment.mapper.PaymentMapper;
import org.mapstruct.factory.Mappers;
import com.drivingschool.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper = Mappers.getMapper(PaymentMapper.class);

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private PaymentService paymentService;

    private PaymentRequest paymentRequest;
    private PaymentPendingRequest paymentPendingRequest;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentRequest = PaymentFixture.paymentRequest();
        paymentPendingRequest = PaymentFixture.paymentPendingRequest();
        payment = PaymentFixture.paymentPending();

        paymentService = new PaymentService(
                paymentRepository,
                paymentMapper,
                kafkaTemplate
        );
    }

    @Test
    void whenProcessPayment_thenReturnsPaymentResponse() {
        // Given
        String transactionId = PaymentFixture.defaultTransactionId();
        Long studentId = PaymentFixture.defaultStudentId();
        Long lessonId = PaymentFixture.defaultLessonId();
        Long paymentId = PaymentFixture.defaultStudentId();
        Payment.PaymentStatus expectedStatus = Payment.PaymentStatus.COMPLETED;
        String kafkaTopic = "payment-processed";

        when(paymentRepository.findByTransactionIdWithLock(transactionId)).thenReturn(Optional.empty());
        when(paymentRepository.findPendingByLessonIdAndStudentId(
                lessonId,
                studentId,
                Payment.PaymentStatus.PENDING))
                .thenReturn(Collections.singletonList(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setId(paymentId);
            saved.setStatus(expectedStatus);
            return saved;
        });

        // When
        PaymentResponse result = paymentService.processPayment(paymentRequest, studentId);

        // Then
        assertNotNull(result);
        assertEquals(paymentId, result.id());
        assertEquals(expectedStatus, result.status());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(kafkaTemplate, times(1)).send(eq(kafkaTopic), anyString(), any(Payment.class));
    }

    @Test
    void whenProcessPaymentWithDuplicateTransactionId_thenThrowsBusinessException() {
        // Given
        String transactionId = PaymentFixture.defaultTransactionId();
        Long existingPaymentId = 2L;
        Payment.PaymentStatus existingStatus = Payment.PaymentStatus.COMPLETED;

        Payment existingPayment = PaymentFixture.payment(existingPaymentId, existingStatus);
        when(paymentRepository.findByTransactionIdWithLock(transactionId))
                .thenReturn(Optional.of(existingPayment));

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.processPayment(paymentRequest, PaymentFixture.defaultStudentId()));

        assertEquals(ErrorCode.DUPLICATE_TRANSACTION.getCode(), exception.getErrorCode());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void whenProcessPaymentWithoutLessonId_thenThrowsBusinessException() {
        // Given
        Long studentId = PaymentFixture.defaultStudentId();
        String transactionId = PaymentFixture.defaultTransactionId();

        PaymentRequest requestWithoutLessonId = new PaymentRequest(
                PaymentFixture.defaultPaymentMethod(),
                transactionId,
                null
        );
        when(paymentRepository.findByTransactionIdWithLock(transactionId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.processPayment(requestWithoutLessonId, studentId));

        assertEquals(ErrorCode.MISSING_LESSON_ID.getCode(), exception.getErrorCode());
    }

    @Test
    void whenProcessPaymentWithNoPendingPayment_thenThrowsBusinessException() {
        // Given
        String transactionId = PaymentFixture.defaultTransactionId();
        Long studentId = PaymentFixture.defaultStudentId();
        Long lessonId = PaymentFixture.defaultLessonId();

        when(paymentRepository.findByTransactionIdWithLock(transactionId)).thenReturn(Optional.empty());
        when(paymentRepository.findPendingByLessonIdAndStudentId(
                lessonId,
                studentId,
                Payment.PaymentStatus.PENDING))
                .thenReturn(Collections.emptyList());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.processPayment(paymentRequest, studentId));

        assertEquals(ErrorCode.NO_PENDING_PAYMENT.getCode(), exception.getErrorCode());
    }

    @Test
    void whenProcessPaymentWithoutTransactionId_thenGeneratesTransactionId() {
        // Given
        Long studentId = PaymentFixture.defaultStudentId();
        Long lessonId = PaymentFixture.defaultLessonId();
        String transactionIdPrefix = "TXN-";

        PaymentRequest requestWithoutTransactionId = PaymentFixture.paymentRequestWithoutTransactionId();
        Payment paymentWithoutTransactionId = PaymentFixture.paymentPending();
        paymentWithoutTransactionId.setTransactionId(null);

        when(paymentRepository.findPendingByLessonIdAndStudentId(
                lessonId,
                studentId,
                Payment.PaymentStatus.PENDING))
                .thenReturn(Collections.singletonList(paymentWithoutTransactionId));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            assertNotNull(saved.getTransactionId());
            assertTrue(saved.getTransactionId().startsWith(transactionIdPrefix));
            return saved;
        });

        // When
        paymentService.processPayment(requestWithoutTransactionId, studentId);

        // Then
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void whenRefundPayment_thenReturnsRefundedPaymentResponse() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();
        Payment.PaymentStatus expectedStatus = Payment.PaymentStatus.REFUNDED;

        Payment completedPayment = PaymentFixture.paymentCompleted();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(completedPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setStatus(expectedStatus);
            return saved;
        });

        // When
        PaymentResponse result = paymentService.refundPayment(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(expectedStatus, result.status());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void whenRefundPaymentWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> paymentService.refundPayment(paymentId));
    }

    @Test
    void whenRefundPaymentWithInvalidStatus_thenThrowsBusinessException() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();

        Payment pendingPayment = PaymentFixture.paymentPending();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(pendingPayment));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.refundPayment(paymentId));

        assertEquals(ErrorCode.INVALID_REFUND_STATUS.getCode(), exception.getErrorCode());
    }

    @Test
    void whenUpdatePaymentStatus_thenReturnsUpdatedPaymentResponse() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();
        Payment.PaymentStatus newStatus = Payment.PaymentStatus.COMPLETED;

        Payment pendingPayment = PaymentFixture.paymentPending();
        pendingPayment.setPaymentMethod(PaymentFixture.defaultPaymentMethod());

        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setStatus(newStatus);
            return saved;
        });

        // When
        PaymentResponse result = paymentService.updatePaymentStatus(paymentId, newStatus);

        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.status());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void whenUpdatePaymentStatusForRefundedPayment_thenThrowsBusinessException() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();
        Payment.PaymentStatus newStatus = Payment.PaymentStatus.COMPLETED;

        Payment refundedPayment = PaymentFixture.paymentRefunded();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(refundedPayment));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.updatePaymentStatus(paymentId, newStatus));

        assertEquals(ErrorCode.INVALID_STATUS_CHANGE.getCode(), exception.getErrorCode());
    }

    @Test
    void whenUpdatePaymentStatusToCompletedWithoutPaymentMethod_thenThrowsBusinessException() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();
        Payment.PaymentStatus newStatus = Payment.PaymentStatus.COMPLETED;

        Payment pendingPayment = PaymentFixture.paymentPending();
        pendingPayment.setPaymentMethod(null);
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(pendingPayment));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.updatePaymentStatus(paymentId, newStatus));

        assertEquals(ErrorCode.MISSING_PAYMENT_METHOD.getCode(), exception.getErrorCode());
    }

    @Test
    void whenGetPaymentById_thenReturnsPaymentResponse() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When
        PaymentResponse result = paymentService.getPaymentById(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(paymentId, result.id());
    }

    @Test
    void whenGetPaymentByIdWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentById(paymentId));
    }

    @Test
    void whenGetStudentPaymentsWithStatus_thenReturnsPaymentsWithStatus() {
        // Given
        Long studentId = PaymentFixture.defaultStudentId();
        Payment.PaymentStatus status = Payment.PaymentStatus.PENDING;
        int expectedPaymentsCount = 1;

        List<Payment> payments = Collections.singletonList(payment);
        when(paymentRepository.findByStudentIdAndStatus(studentId, status))
                .thenReturn(payments);

        // When
        List<PaymentResponse> result = paymentService.getStudentPayments(studentId, status);

        // Then
        assertNotNull(result);
        assertEquals(expectedPaymentsCount, result.size());
    }

    @Test
    void whenGetStudentPaymentsWithoutStatus_thenReturnsAllStudentPayments() {
        // Given
        Long studentId = PaymentFixture.defaultStudentId();
        Payment.PaymentStatus status = null;
        int expectedPaymentsCount = 1;

        List<Payment> payments = Collections.singletonList(payment);
        when(paymentRepository.findByStudentId(studentId)).thenReturn(payments);

        // When
        List<PaymentResponse> result = paymentService.getStudentPayments(studentId, status);

        // Then
        assertNotNull(result);
        assertEquals(expectedPaymentsCount, result.size());
    }

    @Test
    void whenGetStudentBalance_thenReturnsTotalBalance() {
        // Given
        Long studentId = PaymentFixture.defaultStudentId();
        BigDecimal amount1 = new BigDecimal("500.00");
        BigDecimal amount2 = new BigDecimal("300.00");
        BigDecimal expectedBalance = new BigDecimal("800.00");
        Long payment2Id = 2L;

        Payment payment1 = PaymentFixture.paymentCompleted();
        payment1.setAmount(amount1);
        Payment payment2 = PaymentFixture.payment(payment2Id, Payment.PaymentStatus.COMPLETED);
        payment2.setAmount(amount2);

        when(paymentRepository.findByStudentIdAndStatus(
                studentId,
                Payment.PaymentStatus.COMPLETED))
                .thenReturn(List.of(payment1, payment2));

        // When
        BigDecimal balance = paymentService.getStudentBalance(studentId);

        // Then
        assertNotNull(balance);
        assertEquals(expectedBalance, balance);
    }

    @Test
    void whenGetStudentBalanceWithNoPayments_thenReturnsZero() {
        // Given
        Long studentId = PaymentFixture.defaultStudentId();
        BigDecimal expectedBalance = BigDecimal.ZERO;

        when(paymentRepository.findByStudentIdAndStatus(
                studentId,
                Payment.PaymentStatus.COMPLETED))
                .thenReturn(Collections.emptyList());

        // When
        BigDecimal balance = paymentService.getStudentBalance(studentId);

        // Then
        assertNotNull(balance);
        assertEquals(expectedBalance, balance);
    }

    @Test
    void whenCreatePendingPayment_thenReturnsPendingPaymentResponse() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setId(paymentId);
            return saved;
        });

        // When
        PaymentResponse result = paymentService.createPendingPayment(paymentPendingRequest);

        // Then
        assertNotNull(result);
        assertEquals(paymentId, result.id());
        assertEquals(Payment.PaymentStatus.PENDING, result.status());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void whenDeletePendingPayment_thenDeletes() {
        Long paymentId = PaymentFixture.defaultStudentId();
        Payment pending = PaymentFixture.paymentPending();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(pending));

        paymentService.deletePendingPayment(paymentId);

        verify(paymentRepository).delete(pending);
    }

    @Test
    void whenDeletePendingPaymentWithNonExistentId_thenThrowsResourceNotFoundException() {
        Long paymentId = PaymentFixture.defaultStudentId();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.deletePendingPayment(paymentId));
        verify(paymentRepository, never()).delete(any(Payment.class));
    }

    @Test
    void whenDeleteCompletedPayment_thenThrowsBusinessException() {
        Long paymentId = PaymentFixture.defaultStudentId();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(PaymentFixture.paymentCompleted()));

        BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.deletePendingPayment(paymentId));
        assertEquals(ErrorCode.PAYMENT_DELETE_NOT_ALLOWED.getCode(), ex.getErrorCode());
        verify(paymentRepository, never()).delete(any(Payment.class));
    }

    @Test
    void whenDeleteRefundedPayment_thenThrowsBusinessException() {
        Long paymentId = PaymentFixture.defaultStudentId();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(PaymentFixture.paymentRefunded()));

        assertThrows(BusinessException.class, () -> paymentService.deletePendingPayment(paymentId));
        verify(paymentRepository, never()).delete(any(Payment.class));
    }

    @Test
    void whenDeleteFailedPayment_thenThrowsBusinessException() {
        Long paymentId = PaymentFixture.defaultStudentId();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(PaymentFixture.paymentFailed()));

        assertThrows(BusinessException.class, () -> paymentService.deletePendingPayment(paymentId));
        verify(paymentRepository, never()).delete(any(Payment.class));
    }

    @Test
    void whenDeleteCancelledPayment_thenThrowsBusinessException() {
        Long paymentId = PaymentFixture.defaultStudentId();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(PaymentFixture.paymentCancelled()));

        assertThrows(BusinessException.class, () -> paymentService.deletePendingPayment(paymentId));
        verify(paymentRepository, never()).delete(any(Payment.class));
    }

    @Test
    void whenReconcileCancelledLessonPayments_thenCancelsPendingAndRefundsCompleted() {
        Long lessonId = PaymentFixture.defaultLessonId();
        Long studentId = PaymentFixture.defaultStudentId();

        Payment pending = PaymentFixture.paymentPending();
        pending.setLessonId(lessonId);
        pending.setStudentId(studentId);

        Payment completed = PaymentFixture.paymentCompleted();
        completed.setLessonId(lessonId);
        completed.setStudentId(studentId);

        Payment failed = PaymentFixture.paymentFailed();
        failed.setLessonId(lessonId);
        failed.setStudentId(studentId);

        when(paymentRepository.findByLessonIdAndStudentIdWithLock(lessonId, studentId))
                .thenReturn(List.of(pending, completed, failed));
        when(paymentRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = paymentService.reconcilePaymentsForCancelledLesson(lessonId, studentId);

        assertEquals(1, result.cancelledCount());
        assertEquals(1, result.refundedCount());
        assertEquals(Payment.PaymentStatus.CANCELLED, pending.getStatus());
        assertEquals(Payment.PaymentStatus.REFUNDED, completed.getStatus());
        assertEquals(Payment.PaymentStatus.FAILED, failed.getStatus());
        verify(paymentRepository).saveAll(anyList());
    }

    @Test
    void whenReconcileCancelledLessonPaymentsWithNoMatches_thenReturnsZeroActions() {
        Long lessonId = PaymentFixture.defaultLessonId();
        Long studentId = PaymentFixture.defaultStudentId();

        when(paymentRepository.findByLessonIdAndStudentIdWithLock(lessonId, studentId))
                .thenReturn(Collections.emptyList());

        var result = paymentService.reconcilePaymentsForCancelledLesson(lessonId, studentId);

        assertEquals(0, result.cancelledCount());
        assertEquals(0, result.refundedCount());
        verify(paymentRepository, never()).saveAll(anyList());
    }
}
