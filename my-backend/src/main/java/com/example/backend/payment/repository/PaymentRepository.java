package com.example.backend.payment.repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.HotelOwner.domain.Room;
import com.example.backend.HotelOwner.dto.DailySalesDto;
import com.example.backend.payment.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {


    // 특정 소유자의 호텔에서 특정 기간 동안 완료된 결제 금액의 합계를 조회하는 쿼리
    @Query("SELECT COALESCE(SUM(p.basePrice), 0) FROM Payment p " +
           "WHERE p.status = 'COMPLETED' AND p.reservationId IN " +
           "(SELECT r.id FROM Reservation r WHERE r.roomId IN " +
           "(SELECT rm.id FROM Room rm WHERE rm.hotel.owner.id = :ownerId) " +
           "AND r.endDate >= :startDate AND r.endDate < :endDate " +
           "AND r.endDate <= :now)") // ✨ 체크아웃이 완료된 예약만 포함하도록 조건 추가
    long sumCompletedPaymentsByOwnerAndDateRange(
        @Param("ownerId") Long ownerId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("now") Instant now);


    @Query("SELECT new com.example.backend.HotelOwner.dto.DailySalesDto(CAST(r.endDate AS LocalDate), SUM(p.basePrice)) " +
           "FROM Payment p JOIN Reservation r ON p.reservationId = r.id " +
           "WHERE p.status = 'COMPLETED' " +
           "AND r.roomId IN " +
           "(SELECT rm.id FROM Room rm WHERE rm.hotel.owner.id = :ownerId " +
           "AND (:hotelId IS NULL OR rm.hotel.id = :hotelId) " +
           "AND (:roomType IS NULL OR rm.roomType = :roomType)) " +
           "AND r.endDate >= :startDate AND r.endDate < :endDate " +
           "GROUP BY CAST(r.endDate AS LocalDate) " +
           "ORDER BY CAST(r.endDate AS LocalDate)")
    List<DailySalesDto> findDailySalesByOwner(
        @Param("ownerId") Long ownerId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("hotelId") Long hotelId,
        @Param("roomType") Room.RoomType roomType
    );
}
