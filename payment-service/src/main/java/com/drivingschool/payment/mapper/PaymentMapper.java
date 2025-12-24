package com.drivingschool.payment.mapper;

import com.drivingschool.payment.dto.PaymentRequest;
import com.drivingschool.payment.dto.PaymentResponse;
import com.drivingschool.payment.entity.Course;
import com.drivingschool.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public Payment toEntity(PaymentRequest request, Course course) {
        return Payment.builder()
                .studentId(request.getStudentId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(Payment.PaymentStatus.PENDING)
                .course(course)
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
                .invoiceId(payment.getInvoiceId())
                .courseId(payment.getCourse() != null ? payment.getCourse().getId() : null)
                .createdAt(payment.getCreatedAt())
                .build();
    }
}

