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
                .studentId(request.studentId())
                .status(Payment.PaymentStatus.PENDING)
                .lessonId(request.lessonId())
                .build();

        // Set payment method if provided, otherwise use a default for pending payments
        if (request.paymentMethod() != null) {
            payment.setPaymentMethod(request.paymentMethod());
        } else {
            // Default payment method for pending payments (will be set when payment is processed)
            payment.setPaymentMethod(Payment.PaymentMethod.ONLINE);
        }
        
        return payment;
    }

    public Payment toEntityFromPendingRequest(PaymentPendingRequest request) {
        return Payment.builder()
                .studentId(request.studentId())
                .amount(request.amount())
                .status(Payment.PaymentStatus.PENDING)
                .lessonId(request.lessonId())
                .notes(request.notes())
                .build();
    }

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getStudentId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionDate(),
                payment.getTransactionId(),
                payment.getLessonId(),
                payment.getNotes(),
                payment.getCreatedAt(),
                payment.getLastModifiedDate()
        );
    }
}

