package com.drivingschool.payment.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
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
        log.info("Processing payment for student ID: {}, lesson ID: {}", request.getStudentId(), request.getLessonId());

        // Check for duplicate transaction ID with pessimistic lock
        if (request.getTransactionId() != null && !request.getTransactionId().isEmpty()) {
            paymentRepository.findByTransactionIdWithLock(request.getTransactionId())
                    .ifPresent(existing -> {
                        throw new BusinessException(
                                "Payment with transaction ID " + request.getTransactionId() + " already exists",
                                "DUPLICATE_TRANSACTION");
                    });
        }

        Payment payment = null;

        List<Payment> pendingPayments = paymentRepository.findPendingByLessonIdAndStudentId(
                request.getLessonId(), request.getStudentId(), Payment.PaymentStatus.PENDING);

        if (!pendingPayments.isEmpty()) {
            payment = pendingPayments.getFirst();
            log.info("Found existing PENDING payment with ID: {} for lesson ID: {}", payment.getId(), request.getLessonId());

            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setStatus(Payment.PaymentStatus.COMPLETED);

            // Set transaction ID if not already set
            if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
                if (request.getTransactionId() != null && !request.getTransactionId().isEmpty()) {
                    payment.setTransactionId(request.getTransactionId());
                } else {
                    payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                }
            }

            payment.setPaymentMethod(request.getPaymentMethod());

            payment = paymentRepository.save(payment);

        } else {
            log.warn("No existing PENDING payment found for lesson ID: {}", request.getLessonId());
        }
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
    public List<PaymentResponse> getStudentPayments(Long studentId) {
        log.info("Fetching payments for student ID: {}", studentId);
        List<Payment> payments = paymentRepository.findByStudentId(studentId);
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
}

