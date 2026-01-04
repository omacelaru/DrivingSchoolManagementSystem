package com.drivingschool.payment.service;

import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.payment.dto.PaymentRequest;
import com.drivingschool.payment.entity.Payment;
import com.drivingschool.payment.mapper.PaymentMapper;
import com.drivingschool.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest(
                1L,
                Payment.PaymentMethod.CARD,
                null,
                1L
        );

        payment = Payment.builder()
                .id(1L)
                .studentId(1L)
                .amount(new BigDecimal("1000.00"))
                .paymentMethod(Payment.PaymentMethod.CARD)
                .status(Payment.PaymentStatus.COMPLETED)
                .build();
    }

    @Test
    void testProcessPayment_Success() {
        when(paymentMapper.toEntity(any())).thenReturn(payment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        assertDoesNotThrow(() -> {
            paymentService.processPayment(paymentRequest);
        });

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testGetPaymentById_NotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.getPaymentById(1L);
        });
    }
}

