package com.example.backend.hotel_reservation.repository;

import com.example.backend.hotel_reservation.domain.Reservation;
import com.example.backend.hotel_reservation.domain.ReservationStatus;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findTop500ByStatusAndExpiresAtBefore(ReservationStatus status, Instant cutoff);

    @Query("SELECT r FROM Reservation r WHERE r.roomId IN " +
           "(SELECT ro.id FROM Room ro WHERE ro.hotel.owner.id = :ownerId) " +
           "ORDER BY r.createdAt DESC")
    List<Reservation> findAllByHotelOwnerId(@Param("ownerId") Long ownerId);

    // ✅ [추가] 특정 소유자의 호텔에 오늘 체크인하는 예약 목록 조회
    @Query("SELECT r FROM Reservation r WHERE r.room.hotel.owner.id = :ownerId AND r.startDate = :today AND r.status = 'COMPLETED'")
    List<Reservation> findCheckInsForOwnerByDate(@Param("ownerId") Long ownerId, @Param("today") LocalDate today);

    // ✅ [추가] 특정 소유자의 호텔에 오늘 체크아웃하는 예약 목록 조회
    @Query("SELECT r FROM Reservation r WHERE r.room.hotel.owner.id = :ownerId AND r.endDate = :today AND r.status = 'COMPLETED'")
    List<Reservation> findCheckOutsForOwnerByDate(@Param("ownerId") Long ownerId, @Param("today") LocalDate today);

    // ✅ [추가] 특정 소유자의 호텔에 대한 최근 예약 5건 조회
    @Query("SELECT r FROM Reservation r WHERE r.room.hotel.owner.id = :ownerId AND r.status = 'COMPLETED' ORDER BY r.createdAt DESC")
    List<Reservation> findTop5RecentReservationsForOwner(@Param("ownerId") Long ownerId, Pageable pageable);
}
