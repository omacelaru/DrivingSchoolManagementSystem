package com.drivingschool.payment.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.payment.dto.PaymentPendingRequest;
import com.drivingschool.payment.dto.PaymentRequest;
import com.drivingschool.payment.dto.PaymentResponse;
import com.drivingschool.payment.entity.Payment;
import com.drivingschool.payment.fixture.PaymentFixture;
import com.drivingschool.payment.mapper.PaymentMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper = new PaymentMapper();

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
    void testProcessPayment_Success() {
        // Given
        String transactionId = PaymentFixture.defaultTransactionId();
        Long studentId = PaymentFixture.defaultStudentId();
        Long lessonId = PaymentFixture.defaultLessonId();
        Long paymentId = PaymentFixture.defaultStudentId();
        Payment.PaymentStatus expectedStatus = Payment.PaymentStatus.COMPLETED;
        String kafkaTopic = "payment-processed";
        
        when(paymentRepository.findByTransactionIdWithLock(transactionId)).thenReturn(Optional.empty());
        when(paymentRepository.findPendingByLessonIdAndStudentId(
                studentId, 
                lessonId, 
                Payment.PaymentStatus.PENDING))
                .thenReturn(Collections.singletonList(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setId(paymentId);
            saved.setStatus(expectedStatus);
            return saved;
        });

        // When
        PaymentResponse result = paymentService.processPayment(paymentRequest);

        // Then
        assertNotNull(result);
        assertEquals(paymentId, result.id());
        assertEquals(expectedStatus, result.status());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(kafkaTemplate, times(1)).send(eq(kafkaTopic), anyString(), any(Payment.class));
    }

    @Test
    void testProcessPayment_DuplicateTransactionId() {
        // Given
        String transactionId = PaymentFixture.defaultTransactionId();
        Long existingPaymentId = 2L;
        Payment.PaymentStatus existingStatus = Payment.PaymentStatus.COMPLETED;
        String expectedErrorCode = "DUPLICATE_TRANSACTION";
        
        Payment existingPayment = PaymentFixture.payment(existingPaymentId, existingStatus);
        when(paymentRepository.findByTransactionIdWithLock(transactionId))
                .thenReturn(Optional.of(existingPayment));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.processPayment(paymentRequest);
        });

        assertEquals(expectedErrorCode, exception.getErrorCode());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testProcessPayment_MissingLessonId() {
        // Given
        Long studentId = PaymentFixture.defaultStudentId();
        String transactionId = PaymentFixture.defaultTransactionId();
        String expectedErrorCode = "MISSING_LESSON_ID";
        
        PaymentRequest requestWithoutLessonId = new PaymentRequest(
                studentId,
                PaymentFixture.defaultPaymentMethod(),
                transactionId,
                null
        );
        when(paymentRepository.findByTransactionIdWithLock(transactionId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.processPayment(requestWithoutLessonId);
        });

        assertEquals(expectedErrorCode, exception.getErrorCode());
    }

    @Test
    void testProcessPayment_NoPendingPayment() {
        // Given
        String transactionId = PaymentFixture.defaultTransactionId();
        Long studentId = PaymentFixture.defaultStudentId();
        Long lessonId = PaymentFixture.defaultLessonId();
        String expectedErrorCode = "NO_PENDING_PAYMENT";
        
        when(paymentRepository.findByTransactionIdWithLock(transactionId)).thenReturn(Optional.empty());
        when(paymentRepository.findPendingByLessonIdAndStudentId(
                studentId, 
                lessonId, 
                Payment.PaymentStatus.PENDING))
                .thenReturn(Collections.emptyList());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.processPayment(paymentRequest);
        });

        assertEquals(expectedErrorCode, exception.getErrorCode());
    }

    @Test
    void testProcessPayment_GeneratesTransactionId() {
        // Given
        Long studentId = PaymentFixture.defaultStudentId();
        Long lessonId = PaymentFixture.defaultLessonId();
        String transactionIdPrefix = "TXN-";
        
        PaymentRequest requestWithoutTransactionId = PaymentFixture.paymentRequestWithoutTransactionId();
        Payment paymentWithoutTransactionId = PaymentFixture.paymentPending();
        paymentWithoutTransactionId.setTransactionId(null);

        when(paymentRepository.findPendingByLessonIdAndStudentId(
                studentId, 
                lessonId, 
                Payment.PaymentStatus.PENDING))
                .thenReturn(Collections.singletonList(paymentWithoutTransactionId));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            assertNotNull(saved.getTransactionId());
            assertTrue(saved.getTransactionId().startsWith(transactionIdPrefix));
            return saved;
        });

        // When
        paymentService.processPayment(requestWithoutTransactionId);

        // Then
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testRefundPayment_Success() {
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
    void testRefundPayment_NotFound() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.refundPayment(paymentId);
        });
    }

    @Test
    void testRefundPayment_InvalidStatus() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();
        String expectedErrorCode = "INVALID_REFUND_STATUS";
        
        Payment pendingPayment = PaymentFixture.paymentPending();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(pendingPayment));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.refundPayment(paymentId);
        });

        assertEquals(expectedErrorCode, exception.getErrorCode());
    }

    @Test
    void testUpdatePaymentStatus_Success() {
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
    void testUpdatePaymentStatus_RefundedPayment() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();
        Payment.PaymentStatus newStatus = Payment.PaymentStatus.COMPLETED;
        String expectedErrorCode = "INVALID_STATUS_CHANGE";
        
        Payment refundedPayment = PaymentFixture.paymentRefunded();
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(refundedPayment));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.updatePaymentStatus(paymentId, newStatus);
        });

        assertEquals(expectedErrorCode, exception.getErrorCode());
    }

    @Test
    void testUpdatePaymentStatus_MissingPaymentMethod() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();
        Payment.PaymentStatus newStatus = Payment.PaymentStatus.COMPLETED;
        String expectedErrorCode = "MISSING_PAYMENT_METHOD";
        
        Payment pendingPayment = PaymentFixture.paymentPending();
        pendingPayment.setPaymentMethod(null);
        when(paymentRepository.findByIdWithLock(paymentId)).thenReturn(Optional.of(pendingPayment));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.updatePaymentStatus(paymentId, newStatus);
        });

        assertEquals(expectedErrorCode, exception.getErrorCode());
    }

    @Test
    void testGetPaymentById_Success() {
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
    void testGetPaymentById_NotFound() {
        // Given
        Long paymentId = PaymentFixture.defaultStudentId();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.getPaymentById(paymentId);
        });
    }

    @Test
    void testGetStudentPayments_WithStatus() {
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
    void testGetStudentPayments_WithoutStatus() {
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
    void testGetStudentBalance() {
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
    void testGetStudentBalance_NoPayments() {
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
    void testCreatePendingPayment_Success() {
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
}
