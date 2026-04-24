package com.drivingschool.payment.mapper;

import com.drivingschool.common.mapstruct.IgnoreJpaIdAndTimestamps;
import com.drivingschool.payment.dto.PaymentPendingRequest;
import com.drivingschool.payment.dto.PaymentRequest;
import com.drivingschool.payment.dto.PaymentResponse;
import com.drivingschool.payment.entity.Payment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    default Payment toEntity(PaymentRequest request) {
        Payment payment = mapPaymentRequest(request);
        if (payment.getPaymentMethod() == null) {
            payment.setPaymentMethod(Payment.PaymentMethod.ONLINE);
        }
        return payment;
    }

    @IgnoreJpaIdAndTimestamps
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "transactionDate", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "studentId", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @BeanMapping(ignoreUnmappedSourceProperties = "transactionId")
    Payment mapPaymentRequest(PaymentRequest request);

    @IgnoreJpaIdAndTimestamps
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "transactionDate", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    Payment toEntityFromPendingRequest(PaymentPendingRequest request);

    PaymentResponse toResponse(Payment payment);
}
