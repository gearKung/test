package com.example.backend.payment.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.payment.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Payment findByOrderId(String orderId);

    // [추가] 특정 소유자의 호텔에서 특정 기간 동안 완료된 결제 금액의 합계를 조회하는 쿼리
    @Query("SELECT COALESCE(SUM(p.totalPrice), 0) FROM Payment p " +
       "WHERE p.status = 'COMPLETED' AND p.reservationId IN " +
       "(SELECT r.id FROM Reservation r WHERE r.roomId IN " +
       "(SELECT rm.id FROM Room rm WHERE rm.hotel.owner.id = :ownerId)) " +
       "AND p.createdAt BETWEEN :startDate AND :endDate")
    public long sumCompletedPaymentsByOwnerAndDateRange(
        @Param("ownerId") Long ownerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
}
