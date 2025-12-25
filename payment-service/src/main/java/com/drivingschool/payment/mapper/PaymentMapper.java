package com.drivingschool.payment.mapper;

import com.drivingschool.payment.dto.PaymentRequest;
import com.drivingschool.payment.dto.PaymentResponse;
import com.drivingschool.payment.entity.Course;
import com.drivingschool.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public Payment toEntity(PaymentRequest request, Course course) {
        Payment payment = new Payment();
        payment.setStudentId(request.getStudentId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setCourse(course);
        return payment;
    }

    public PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setStudentId(payment.getStudentId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setTransactionDate(payment.getTransactionDate());
        response.setInvoiceId(payment.getInvoiceId());
        if (payment.getCourse() != null) {
            response.setCourseId(payment.getCourse().getId());
        }
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
}
