package com.drivingschool.payment.mapper;

import com.drivingschool.payment.dto.PaymentRequest;
import com.drivingschool.payment.dto.PaymentResponse;
import com.drivingschool.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public Payment toEntity(PaymentRequest request) {
        Payment payment = Payment.builder()
                .studentId(request.getStudentId())
                .amount(request.getAmount())
                .status(Payment.PaymentStatus.PENDING)
                .courseId(request.getCourseId())
                .lessonId(request.getLessonId())
                .notes(request.getNotes())
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

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .studentId(payment.getStudentId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionDate(payment.getTransactionDate())
                .transactionId(payment.getTransactionId())
                .courseId(payment.getCourseId())
                .lessonId(payment.getLessonId())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .lastModifiedDate(payment.getLastModifiedDate())
                .build();
    }
}

