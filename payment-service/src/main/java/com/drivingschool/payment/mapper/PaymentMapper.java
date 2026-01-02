package com.drivingschool.payment.mapper;

import com.drivingschool.payment.dto.PaymentPendingRequest;
import com.drivingschool.payment.dto.PaymentRequest;
import com.drivingschool.payment.dto.PaymentResponse;
import com.drivingschool.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public Payment toEntity(PaymentRequest request) {
        Payment payment = Payment.builder()
                .studentId(request.getStudentId())
                .status(Payment.PaymentStatus.PENDING)
                .lessonId(request.getLessonId())
                .build();

        // Set payment method if provided, otherwise use a default for pending payments
        if (request.getPaymentMethod() != null) {
            payment.setPaymentMethod(request.getPaymentMethod());
        } else {
            // Default payment method for pending payments (will be set when payment is processed)
            payment.setPaymentMethod(Payment.PaymentMethod.ONLINE);
        }
        
        return payment;
    }

    public Payment toEntityFromPendingRequest(PaymentPendingRequest request) {
        return Payment.builder()
                .studentId(request.getStudentId())
                .amount(request.getAmount())
                .status(Payment.PaymentStatus.PENDING)
                .lessonId(request.getLessonId())
                .notes(request.getNotes())
                .build();
    }

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .studentId(payment.getStudentId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionDate(payment.getTransactionDate())
                .transactionId(payment.getTransactionId())
                .lessonId(payment.getLessonId())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .lastModifiedDate(payment.getLastModifiedDate())
                .build();
    }
}

