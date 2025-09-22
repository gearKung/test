package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.Amenity;
import com.example.backend.domain.Hotel;
import com.example.backend.domain.HotelAmenity;
import com.example.backend.domain.HotelImage;
import com.example.backend.domain.Room;
import com.example.backend.domain.User;
import com.example.backend.dto.HotelDto;
import com.example.backend.dto.RoomDto;
import com.example.backend.repository.AmenityRepository;
import com.example.backend.repository.HotelAmenityRepository;
import com.example.backend.repository.HotelImageRepository;
import com.example.backend.repository.HotelRepository;
import com.example.backend.repository.RoomRepository;
import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelService {
    
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final AmenityRepository amenityRepository;
    private final HotelAmenityRepository hotelAmenityRepository;
    private final HotelImageRepository hotelImageRepository;

    @Transactional
    public Hotel createHotel(HotelDto hotelDto, List<String> imageUrls, List<Long> amenityIds, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다."));

        Hotel hotel = Hotel.builder()
                .owner(owner)
                .name(hotelDto.getName())
                .businessId(hotelDto.getBusinessId())
                .address(hotelDto.getAddress())
                .starRating(hotelDto.getStarRating())
                .description(hotelDto.getDescription())
                .country(hotelDto.getCountry())
                .status(Hotel.Status.APPROVED)
                .images(new ArrayList<>())
                .hotelAmenities(new ArrayList<>())
                .build();

        // 이미지 정보 저장
        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<HotelImage> hotelImages = imageUrls.stream()
                    .map(url -> HotelImage.builder().hotel(hotel).url(url).build())
                    .collect(Collectors.toList());
            hotel.getImages().addAll(hotelImages);
        }

        Hotel savedHotel = hotelRepository.save(hotel);

        // 편의시설 정보 저장
        if (amenityIds != null && !amenityIds.isEmpty()) {
            List<Amenity> amenities = amenityRepository.findAllById(amenityIds);
            List<HotelAmenity> hotelAmenities = amenities.stream()
                    .map(amenity -> HotelAmenity.builder().hotel(savedHotel).amenity(amenity).build())
                    .collect(Collectors.toList());
            hotelAmenityRepository.saveAll(hotelAmenities);
        }

        return savedHotel;
    }

    // 업주 본인 호텔 목록 조회
    @Transactional(readOnly = true)
    public List<Hotel> getHotelsByOwner(Long ownerId) {
        return hotelRepository.findByOwnerIdWithDetails(ownerId);
    }

    // 호텔 상세 조회
    @Transactional(readOnly = true)
    public Hotel getHotel(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("호텔을 찾을 수 없습니다. id=" + id));
    }

    // 호텔 삭제
    @Transactional
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new IllegalArgumentException("호텔이 존재하지 않습니다. id=" + id);
        }
        // HotelAmenity 수동 삭제 (Cascade 설정에 따라 불필요할 수 있으나 명시적으로 처리)
        hotelAmenityRepository.deleteByHotelId(id);
        hotelRepository.deleteById(id);
    }
    
    @Transactional
    public Hotel updateHotel(Long id, HotelDto hotelDto, List<String> imageUrls, List<Long> amenityIds) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("호텔을 찾을 수 없습니다. id=" + id));

        hotel.setName(hotelDto.getName());
        hotel.setAddress(hotelDto.getAddress());
        hotel.setStarRating(hotelDto.getStarRating());
        hotel.setCountry(hotelDto.getCountry());
        hotel.setDescription(hotelDto.getDescription());
        hotel.setBusinessId(hotelDto.getBusinessId());

        // 이미지 업데이트 (기존 이미지 모두 삭제 후 새로 추가)
        hotel.getImages().clear();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<HotelImage> newImages = imageUrls.stream()
                    .map(url -> HotelImage.builder().hotel(hotel).url(url).build())
                    .collect(Collectors.toList());
            hotel.getImages().addAll(newImages);
        }

        // 편의시설 업데이트 (기존 연결 모두 삭제 후 새로 추가)
        hotelAmenityRepository.deleteByHotelId(hotel.getId());
        if (amenityIds != null && !amenityIds.isEmpty()) {
            List<Amenity> amenities = amenityRepository.findAllById(amenityIds);
            List<HotelAmenity> hotelAmenities = amenities.stream()
                .map(amenity -> HotelAmenity.builder().hotel(hotel).amenity(amenity).build())
                .collect(Collectors.toList());
            hotelAmenityRepository.saveAll(hotelAmenities);
        }

        return hotelRepository.save(hotel);
    }

    @Transactional(readOnly = true)
    public List<RoomDto> findRoomsByHotelId(Long hotelId) {
        // RoomRepository를 통해 Room 엔티티 목록을 조회
        List<Room> rooms = roomRepository.findByHotelId(hotelId);
        
        // 조회된 엔티티 목록을 DTO 목록으로 변환하여 반환
        return rooms.stream()
            .map(RoomDto::fromEntity)
            .collect(Collectors.toList());
    }

}
