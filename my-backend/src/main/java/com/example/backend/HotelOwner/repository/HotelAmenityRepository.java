package com.example.backend.HotelOwner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.HotelOwner.domain.HotelAmenity;

@Repository
public interface HotelAmenityRepository extends JpaRepository<HotelAmenity, Long> {
    // 특정 호텔에 연결된 모든 편의시설 정보를 삭제하기 위해 사용
    void deleteByHotelId(Long hotelId);
}
