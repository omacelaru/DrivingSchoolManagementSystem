package com.drivingschool.scheduling.client;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.scheduling.dto.PaymentRequest;
import com.drivingschool.scheduling.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "${payment.service.url}")
public interface PaymentClient {
    @PostMapping("/api/payments/pending")
    ApiResult<PaymentResponse> createPendingPayment(@RequestBody PaymentRequest request);
}

