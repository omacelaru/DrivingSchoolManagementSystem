package com.drivingschool.payment.repository;

import com.drivingschool.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentId(Long studentId);
    List<Payment> findByStudentIdAndStatus(Long studentId, Payment.PaymentStatus status);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdWithLock(@Param("id") Long id);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    Optional<Payment> findByTransactionIdWithLock(@Param("transactionId") String transactionId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.lessonId = :lessonId AND p.studentId = :studentId AND p.status = :status ORDER BY p.createdAt DESC")
    List<Payment> findPendingByLessonIdAndStudentId(@Param("lessonId") Long lessonId, @Param("studentId") Long studentId, @Param("status") Payment.PaymentStatus status);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.studentId = :studentId AND p.status = :status AND p.amount = :amount ORDER BY p.createdAt DESC")
    List<Payment> findPendingByStudentIdAndAmount(@Param("studentId") Long studentId, @Param("status") Payment.PaymentStatus status, @Param("amount") java.math.BigDecimal amount);
}

