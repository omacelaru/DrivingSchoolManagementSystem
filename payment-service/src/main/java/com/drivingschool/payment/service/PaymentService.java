package com.drivingschool.payment.service;

import com.drivingschool.common.exception.BusinessException;
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

        // Check for duplicate transaction ID with pessimistic lock
        if (request.transactionId() != null && !request.transactionId().isEmpty()) {
            paymentRepository.findByTransactionIdWithLock(request.transactionId())
                    .ifPresent(existing -> {
                        throw new BusinessException(
                                "Payment with transaction ID " + request.transactionId() + " already exists",
                                "DUPLICATE_TRANSACTION");
                    });
        }

        Payment payment = null;

        // Try to find an existing PENDING payment for this lesson
        if (request.lessonId() != null) {
            List<Payment> pendingPayments = paymentRepository.findPendingByLessonIdAndStudentId(
                    request.lessonId(), request.studentId(), Payment.PaymentStatus.PENDING);

            if (!pendingPayments.isEmpty()) {
                payment = pendingPayments.getFirst();
                log.info("Found existing PENDING payment with ID: {} for lesson ID: {}", payment.getId(), request.lessonId());
            }
        }

        // If no pending payment found, throw exception
        if (payment == null) {
            throw new BusinessException(
                    "No pending payment found for lesson ID: " + request.lessonId() + " and student ID: " + request.studentId() + ". Please book a lesson first.",
                    "NO_PENDING_PAYMENT");
        }

        // Update payment with request data
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus(Payment.PaymentStatus.COMPLETED);

        // Set transaction ID if not already set
        if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
            if (request.transactionId() != null && !request.transactionId().isEmpty()) {
                payment.setTransactionId(request.transactionId());
            } else {
                payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            }
        }

        payment = paymentRepository.save(payment);

        // Publish event to Kafka
        kafkaTemplate.send("payment-processed", payment.getId().toString(), payment);
        log.info("Payment processed with ID: {} (status: {})", payment.getId(), payment.getStatus());

        return paymentMapper.toResponse(payment);
    }

    public PaymentResponse refundPayment(Long id) {
        log.info("Processing refund for payment ID: {}", id);

        // Use pessimistic lock to prevent concurrent refunds
        Payment payment = paymentRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));

        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new BusinessException(
                    "Only completed payments can be refunded. Current status: " + payment.getStatus(),
                    "INVALID_REFUND_STATUS");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        log.info("Payment refunded with ID: {}", payment.getId());
        return paymentMapper.toResponse(payment);
    }

    public PaymentResponse updatePaymentStatus(Long id, Payment.PaymentStatus newStatus) {
        log.info("Updating payment status for ID: {} to {}", id, newStatus);

        // Use pessimistic lock to prevent concurrent status updates
        Payment payment = paymentRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));

        if (payment.getStatus() == Payment.PaymentStatus.REFUNDED && newStatus != Payment.PaymentStatus.REFUNDED) {
            throw new BusinessException(
                    "Cannot change status of a refunded payment",
                    "INVALID_STATUS_CHANGE");
        }

        payment.setStatus(newStatus);
        payment = paymentRepository.save(payment);

        log.info("Payment status updated for ID: {}", payment.getId());
        return paymentMapper.toResponse(payment);
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
        List<Payment> payments;
        if (status != null) {
            payments = paymentRepository.findByStudentIdAndStatus(studentId, status);
        } else {
            payments = paymentRepository.findByStudentId(studentId);
        }
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

