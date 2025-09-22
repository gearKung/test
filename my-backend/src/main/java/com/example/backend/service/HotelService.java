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
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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
        log.info("3. [HotelService] getHotelsByOwner 호출됨, ownerId: {}", ownerId);
        List<Hotel> hotels = hotelRepository.findByOwnerIdWithDetails(ownerId);
        log.info("   [HotelService] DB 조회 결과 (이미지 포함), {}개의 호텔을 찾음", hotels.size());

        // ✅ [추가] 편의시설 정보를 별도로 로딩합니다.
        // N+1 문제를 방지하기 위해 stream을 사용하여 각 호텔의 편의시설을 초기화합니다.
        hotels.forEach(hotel -> {
            if (hotel.getHotelAmenities() != null) {
                // LAZY 로딩된 컬렉션에 접근하여 데이터를 로드합니다.
                hotel.getHotelAmenities().size(); 
            }
        });
        
        log.info("   [HotelService] 편의시설 정보 로딩 완료");
        return hotels;
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
        log.info("3. [Service-삭제] deleteHotel 호출됨, 호텔 ID: {}", id);

        Hotel hotel = hotelRepository.findById(id)
            .orElseThrow(() -> {
                log.error("   [Service-삭제] 삭제할 호텔이 존재하지 않음, ID: {}", id);
                return new IllegalArgumentException("호텔이 존재하지 않습니다. id=" + id);
            });

        // 2. [핵심] 호텔에 속한 모든 객실(Room)을 먼저 삭제합니다.
        // 이렇게 하면 Room과 관련된 다른 데이터(예: RoomImage)도 Cascade 설정에 따라 함께 삭제됩니다.
        List<Room> roomsToDelete = roomRepository.findByHotel(hotel);
        if (!roomsToDelete.isEmpty()) {
            log.info("   [Service-삭제] 호텔에 속한 {}개의 객실을 먼저 삭제합니다.", roomsToDelete.size());
            roomRepository.deleteAll(roomsToDelete);
        }

        // 3. 호텔에 연결된 편의시설 정보를 삭제합니다.
        log.info("   [Service-삭제] 연결된 편의시설 정보 삭제 시작");
        hotelAmenityRepository.deleteByHotelId(hotel.getId());
        
        // 4. 마지막으로 호텔을 삭제합니다.
        // (호텔 이미지는 Hotel의 Cascade 설정에 따라 자동으로 삭제됩니다.)
        log.info("   [Service-삭제] 호텔 본체 삭제 시작");
        hotelRepository.delete(hotel);
        
        log.info("   [Service-삭제] 호텔 ID {} 삭제 완료", id);
    }
    
    @Transactional
    public HotelDto updateHotel(Long id, HotelDto hotelDto, List<String> imageUrls, List<Long> amenityIds) {
        log.info("3. [Service-수정] updateHotel 호출됨, 호텔 ID: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("호텔을 찾을 수 없습니다. id=" + id));
        log.info("   [Service-수정] 수정할 호텔을 찾음: {}", hotel.getName());

        // 1. 호텔 기본 정보 업데이트
        hotel.setName(hotelDto.getName());
        hotel.setAddress(hotelDto.getAddress());
        hotel.setStarRating(hotelDto.getStarRating());
        hotel.setCountry(hotelDto.getCountry());
        hotel.setDescription(hotelDto.getDescription());
        hotel.setBusinessId(hotelDto.getBusinessId());
        log.info("   [Service-수정] 호텔 기본 정보 업데이트 완료");

        // 2. 이미지 정보 업데이트
        hotel.getImages().clear(); // 엔티티의 이미지 리스트를 비움
        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<HotelImage> newImages = imageUrls.stream()
                    .map(url -> HotelImage.builder().hotel(hotel).url(url).build())
                    .collect(Collectors.toList());
            hotel.getImages().addAll(newImages); // 엔티티의 이미지 리스트에 새로 추가
        }
        log.info("   [Service-수정] 이미지 정보 업데이트 완료. 새 이미지 수: {}", imageUrls != null ? imageUrls.size() : 0);

        // 3. 편의시설 정보 업데이트 (JPA 방식)
        hotel.getHotelAmenities().clear(); // 엔티티의 편의시설 리스트를 비움 (orphanRemoval=true가 DB 삭제를 처리)
        log.info("   [Service-수정] 기존 편의시설 연결을 모두 제거했습니다.");

        if (amenityIds != null && !amenityIds.isEmpty()) {
            List<Amenity> amenities = amenityRepository.findAllById(amenityIds);
            List<HotelAmenity> newHotelAmenities = amenities.stream()
                .map(amenity -> HotelAmenity.builder().hotel(hotel).amenity(amenity).build())
                .collect(Collectors.toList());
            hotel.getHotelAmenities().addAll(newHotelAmenities); // 엔티티의 편의시설 리스트에 새로 추가
            log.info("   [Service-수정] {}개의 새 편의시설 연결을 추가했습니다.", newHotelAmenities.size());
        }

        // 4. 호텔 엔티티를 저장하면 모든 변경사항(이미지, 편의시설 포함)이 트랜잭션 커밋 시 DB에 반영됩니다.
        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("   [Service-수정] 호텔 저장 완료. 트랜잭션 커밋 대기 중...");

        // 5. Controller의 LazyInitializationException 방지를 위해 Service단에서 DTO로 변환하여 반환
        return HotelDto.fromEntity(savedHotel);
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
