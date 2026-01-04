package com.drivingschool.payment.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.payment.dto.PaymentPendingRequest;
import com.drivingschool.payment.dto.PaymentRequest;
import com.drivingschool.payment.dto.PaymentResponse;
import com.drivingschool.payment.entity.Payment;
import com.drivingschool.payment.mapper.PaymentMapper;
import com.drivingschool.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for student ID: {}, lesson ID: {}", request.studentId(), request.lessonId());

        validateTransactionId(request.transactionId());
        Payment payment = findPendingPayment(request);
        updatePaymentForCompletion(payment, request);
        payment = paymentRepository.save(payment);
        publishPaymentProcessedEvent(payment);

        log.info("Payment processed with ID: {} (status: {})", payment.getId(), payment.getStatus());
        return paymentMapper.toResponse(payment);
    }

    private void validateTransactionId(String transactionId) {
        if (transactionId != null && !transactionId.isEmpty()) {
            paymentRepository.findByTransactionIdWithLock(transactionId)
                    .ifPresent(existing -> {
                        throw new BusinessException(
                                "Payment with transaction ID " + transactionId + " already exists",
                                ErrorCode.DUPLICATE_TRANSACTION);
                    });
        }
    }

    private Payment findPendingPayment(PaymentRequest request) {
        if (request.lessonId() == null) {
            throw new BusinessException(
                    "Lesson ID is required to process payment",
                    ErrorCode.MISSING_LESSON_ID);
        }

        List<Payment> pendingPayments = paymentRepository.findPendingByLessonIdAndStudentId(
                request.lessonId(), request.studentId(), Payment.PaymentStatus.PENDING);

        if (pendingPayments.isEmpty()) {
            throw new BusinessException(
                    "No pending payment found for lesson ID: " + request.lessonId() + " and student ID: " + request.studentId() + ". Please book a lesson first.",
                    ErrorCode.NO_PENDING_PAYMENT);
        }

        Payment payment = pendingPayments.getFirst();
        log.info("Found existing PENDING payment with ID: {} for lesson ID: {}", payment.getId(), request.lessonId());
        return payment;
    }

    private void updatePaymentForCompletion(Payment payment, PaymentRequest request) {
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        setTransactionIdIfNeeded(payment, request.transactionId());
    }

    private void setTransactionIdIfNeeded(Payment payment, String requestTransactionId) {
        if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
            String transactionId = requestTransactionId != null && !requestTransactionId.isEmpty()
                    ? requestTransactionId
                    : generateTransactionId();
            payment.setTransactionId(transactionId);
        }
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void publishPaymentProcessedEvent(Payment payment) {
        kafkaTemplate.send("payment-processed", payment.getId().toString(), payment);
    }

    public PaymentResponse refundPayment(Long id) {
        log.info("Processing refund for payment ID: {}", id);

        Payment payment = findPaymentWithLock(id);
        validatePaymentCanBeRefunded(payment);
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        log.info("Payment refunded with ID: {}", payment.getId());
        return paymentMapper.toResponse(payment);
    }

    private Payment findPaymentWithLock(Long id) {
        return paymentRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    private void validatePaymentCanBeRefunded(Payment payment) {
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new BusinessException(
                    "Only completed payments can be refunded. Current status: " + payment.getStatus(),
                    ErrorCode.INVALID_REFUND_STATUS);
        }
    }

    public PaymentResponse updatePaymentStatus(Long id, Payment.PaymentStatus newStatus) {
        log.info("Updating payment status for ID: {} to {}", id, newStatus);

        Payment payment = findPaymentWithLock(id);
        validateStatusChange(payment, newStatus);
        payment.setStatus(newStatus);
        payment = paymentRepository.save(payment);

        log.info("Payment status updated for ID: {}", payment.getId());
        return paymentMapper.toResponse(payment);
    }

    private void validateStatusChange(Payment payment, Payment.PaymentStatus newStatus) {
        if (payment.getStatus() == Payment.PaymentStatus.REFUNDED && newStatus != Payment.PaymentStatus.REFUNDED) {
            throw new BusinessException(
                    "Cannot change status of a refunded payment",
                    ErrorCode.INVALID_STATUS_CHANGE);
        }

        if (newStatus == Payment.PaymentStatus.COMPLETED && payment.getPaymentMethod() == null) {
            throw new BusinessException(
                    "Payment method is required when status is COMPLETED",
                    ErrorCode.MISSING_PAYMENT_METHOD);
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        log.info("Fetching payment with ID: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getStudentPayments(Long studentId, Payment.PaymentStatus status) {
        log.info("Fetching payments for student ID: {} with status filter: {}", studentId, status);
        List<Payment> payments = findPaymentsByStudentAndStatus(studentId, status);
        return mapToResponseList(payments);
    }

    private List<Payment> findPaymentsByStudentAndStatus(Long studentId, Payment.PaymentStatus status) {
        return status != null
                ? paymentRepository.findByStudentIdAndStatus(studentId, status)
                : paymentRepository.findByStudentId(studentId);
    }

    private List<PaymentResponse> mapToResponseList(List<Payment> payments) {
        return payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getStudentBalance(Long studentId) {
        log.info("Calculating balance for student ID: {}", studentId);
        List<Payment> payments = paymentRepository.findByStudentIdAndStatus(
                studentId, Payment.PaymentStatus.COMPLETED);
        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public PaymentResponse createPendingPayment(PaymentPendingRequest request) {
        log.info("Creating pending payment for student ID: {}, lesson ID: {}, amount: {}",
                request.studentId(), request.lessonId(), request.amount());

        Payment payment = paymentMapper.toEntityFromPendingRequest(request);
        payment = paymentRepository.save(payment);

        log.info("Pending payment created with ID: {}", payment.getId());
        return paymentMapper.toResponse(payment);
    }
}

