package com.example.backend.HotelOwner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.HotelOwner.domain.Amenity;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    
}