package com.example.backend.hotel_reservation.repository;

import com.example.backend.hotel_reservation.domain.Reservation;
import com.example.backend.hotel_reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findTop500ByStatusAndExpiresAtBefore(ReservationStatus status, Instant cutoff);

    @Query("SELECT r FROM Reservation r WHERE r.roomId IN " +
           "(SELECT ro.id FROM Room ro WHERE ro.hotel.owner.id = :ownerId) " +
           "ORDER BY r.createdAt DESC")
    List<Reservation> findAllByHotelOwnerId(@Param("ownerId") Long ownerId);
}
