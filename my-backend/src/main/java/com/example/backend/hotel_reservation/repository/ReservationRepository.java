package com.example.backend.hotel_reservation.repository;

import com.example.backend.hotel_reservation.domain.Reservation;
import com.example.backend.hotel_reservation.domain.ReservationStatus;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findTop500ByStatusAndExpiresAtBefore(ReservationStatus status, Instant cutoff);

    @Query("SELECT r FROM Reservation r WHERE r.roomId IN " +
           "(SELECT ro.id FROM Room ro WHERE ro.hotel.owner.id = :ownerId) " +
           "ORDER BY r.createdAt DESC")
    List<Reservation> findAllByHotelOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT r FROM Reservation r WHERE r.roomId IN (SELECT ro.id FROM Room ro WHERE ro.hotel.owner.id = :ownerId) AND r.startDate >= :startOfDay AND r.startDate < :endOfDay AND r.status = 'COMPLETED'")
    List<Reservation> findCheckInsForOwnerByDateRange(@Param("ownerId") Long ownerId, @Param("startOfDay") Instant startOfDay, @Param("endOfDay") Instant endOfDay);

    @Query("SELECT r FROM Reservation r WHERE r.roomId IN (SELECT ro.id FROM Room ro WHERE ro.hotel.owner.id = :ownerId) AND r.endDate >= :startOfDay AND r.endDate < :endOfDay AND r.status = 'COMPLETED'")
    List<Reservation> findCheckOutsForOwnerByDateRange(@Param("ownerId") Long ownerId, @Param("startOfDay") Instant startOfDay, @Param("endOfDay") Instant endOfDay);
    
    @Query("SELECT r FROM Reservation r WHERE r.roomId IN (SELECT ro.id FROM Room ro WHERE ro.hotel.owner.id = :ownerId) AND r.status = 'COMPLETED' AND r.createdAt >= :startDate ORDER BY r.createdAt DESC")
    List<Reservation> findRecentReservationsForOwner(@Param("ownerId") Long ownerId, @Param("startDate") LocalDateTime startDate, Pageable pageable);
}