package com.example.backend.HotelOwner.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.HotelOwner.domain.Hotel;
import com.example.backend.HotelOwner.domain.Room;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // 방 종류별 조회 (예: 디럭스룸)
    List<Room> findByRoomType(Room.RoomType roomType);
    
    // 특정 호텔의 모든 객실 가져오기
    // [핵심 수정] JPQL을 사용하여 hotelId로 Room을 찾는 쿼리를 명시적으로 작성
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.hotel WHERE r.hotel.id = :hotelId")
    List<Room> findByHotelId(@Param("hotelId") Long hotelId);

    List<Room> findByHotel(Hotel hotel);

    boolean existsByIdAndHotelOwnerEmail(Long roomId, String ownerEmail);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.hotel LEFT JOIN FETCH r.images WHERE r.hotel.id = :hotelId")
    List<Room> findByHotelIdWithImages(@Param("hotelId") Long hotelId);

    @Query("SELECT r.hotel.id FROM Room r WHERE r.id = :roomId")
    Long findHotelIdByRoomId(@Param("roomId") Long roomId);

}
