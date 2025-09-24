package com.example.backend.HotelOwner.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.HotelOwner.domain.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByOwnerId(Long ownerId);

    boolean existsByIdAndOwnerEmail(Long id, String ownerEmail);

    @Query("SELECT DISTINCT h FROM Hotel h LEFT JOIN FETCH h.images WHERE h.owner.id = :ownerId")
    List<Hotel> findByOwnerIdWithDetails(@Param("ownerId") Long ownerId);
}
