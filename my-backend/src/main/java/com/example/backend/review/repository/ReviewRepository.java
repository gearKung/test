package com.example.backend.review.repository;

import com.example.backend.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.hotel h WHERE h.owner.id = :ownerId ORDER BY r.createdAt DESC")
    List<Review> findByHotelOwnerId(@Param("ownerId") Long ownerId);
}