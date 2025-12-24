package com.drivingschool.payment.repository;

import com.drivingschool.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentId(Long studentId);
    List<Payment> findByStudentIdAndStatus(Long studentId, Payment.PaymentStatus status);
}

