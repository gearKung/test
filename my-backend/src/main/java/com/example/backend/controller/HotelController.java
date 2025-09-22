package com.example.backend.controller;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.service.AmenityService;
import com.example.backend.service.FileStorageService;
import com.example.backend.service.HotelService;
import com.example.backend.service.RoomService;

import io.jsonwebtoken.Claims;

import com.example.backend.config.JwtUtil;
import com.example.backend.domain.Amenity;
import com.example.backend.domain.Hotel;
import com.example.backend.domain.HotelImage;
import com.example.backend.domain.Room;
import com.example.backend.dto.AmenityDto;
import com.example.backend.dto.HotelDto;
import com.example.backend.dto.RoomDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final JwtUtil jwtUtil;
    private final FileStorageService fileStorageService;
    private final AmenityService amenityService;

    @GetMapping("/amenities")
    public ResponseEntity<List<AmenityDto>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAllAmenities());
    }

    // 호텔 등록  -- 나중에 승인요청 보내기로 바꿀 예정
    @PostMapping
    public ResponseEntity<HotelDto> createHotel(
            @RequestPart("hotel") HotelDto hotelDto, // 프론트에서 'hotel' key 값으로 JSON 전송
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader("Authorization") String authHeader) {

        Long ownerId = getUserIdFromToken(authHeader);

        List<String> imageUrls = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            imageUrls = files.stream()
                    .map(fileStorageService::store)
                    .collect(Collectors.toList());
        }

        Hotel savedHotel = hotelService.createHotel(hotelDto, imageUrls, hotelDto.getAmenityIds(), ownerId);
        return ResponseEntity.ok(toDto(savedHotel));
    }

    // 업주별 호텔 목록 조회
    @GetMapping("/my-hotels")
    public ResponseEntity<List<HotelDto>> getMyHotels(@RequestHeader("Authorization") String authHeader) {
        Long ownerId = getUserIdFromToken(authHeader);
        List<Hotel> hotels = hotelService.getHotelsByOwner(ownerId);
        List<HotelDto> hotelDtos = hotels.stream()
                .map(HotelDto::fromEntity) // DTO 변환 메소드 사용
                .collect(Collectors.toList());
        return ResponseEntity.ok(hotelDtos);
    }

    // 호텔 상세보기
    @GetMapping("/{id}")
    public ResponseEntity<HotelDto> getHotel(@PathVariable Long id) {
        Hotel hotel = hotelService.getHotel(id);
        return ResponseEntity.ok(HotelDto.fromEntity(hotel));
    }

    // 호텔 수정
   @PostMapping("/{id}")
    public ResponseEntity<HotelDto> updateHotel(
            @PathVariable Long id,
            @RequestPart("hotel") HotelDto hotelDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader("Authorization") String authHeader) {

        Long ownerId = getUserIdFromToken(authHeader);
        
        // (권한 체크 로직 필요)
        Hotel existingHotel = hotelService.getHotel(id);
        if (!existingHotel.getOwner().getId().equals(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<String> imageUrls = new ArrayList<>();
        // 기존 이미지 URL 유지
        if (hotelDto.getImageUrls() != null) {
            imageUrls.addAll(hotelDto.getImageUrls());
        }
        // 새 파일이 있으면 저장하고 URL 목록에 추가
        if (files != null && !files.isEmpty()) {
            List<String> newImageUrls = files.stream()
                    .map(fileStorageService::store)
                    .collect(Collectors.toList());
            imageUrls.addAll(newImageUrls);
        }

        Hotel updatedHotel = hotelService.updateHotel(id, hotelDto, imageUrls, hotelDto.getAmenityIds());
        return ResponseEntity.ok(toDto(updatedHotel));
    }

    // 호텔 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {

        Long ownerId = getUserIdFromToken(authHeader);

        Hotel existingHotel = hotelService.getHotel(id);
        if (!existingHotel.getOwner().getId().equals(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        

        // (권한 체크 로직 추가 권장)
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    // ----- 객실관리-----

    //특정 호텔의 모든 객실 조회
    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<RoomDto>> getRoomsByHotel(@PathVariable Long hotelId) {
        List<Room> rooms = roomService.findByHotelId(hotelId);
        List<RoomDto> roomDtos = rooms.stream()
                                    .map(RoomDto::fromEntity)
                                    .collect(Collectors.toList());
        return ResponseEntity.ok(roomDtos);
    }

    // 특정 호텔에 객실 등록
    @PostMapping("/{hotelId}/rooms")
    public ResponseEntity<RoomDto> createRoom(
            @PathVariable Long hotelId,
            @RequestPart("room") RoomDto roomDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        List<String> imageUrls = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            imageUrls = files.stream()
                    .map(fileStorageService::store)
                    .collect(Collectors.toList());
        }

        Room newRoom = roomService.createRoom(hotelId, roomDto, imageUrls, userDetails.getUsername());
        return ResponseEntity.ok(RoomDto.fromEntity(newRoom));
    }


    // 객실 수정
    @PostMapping("/rooms/{roomId}")
    public ResponseEntity<RoomDto> updateRoom(
            @PathVariable Long roomId,
            @RequestPart("room") RoomDto roomDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        List<String> imageUrls = new ArrayList<>();
        if (roomDto.getImageUrls() != null) {
            imageUrls.addAll(roomDto.getImageUrls());
        }
        if (files != null && !files.isEmpty()) {
            imageUrls.addAll(files.stream()
                .map(fileStorageService::store)
                .collect(Collectors.toList()));
        }

        Room updatedRoom = roomService.updateRoom(roomId, roomDto, imageUrls, userDetails.getUsername());
        return ResponseEntity.ok(RoomDto.fromEntity(updatedRoom));
    }

    // 객실 삭제
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        roomService.deleteRoom(roomId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // 토큰에서 userId를 추출하는 헬퍼메서드
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 인증 헤더입니다.");
        }
        String token = authHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);

        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            throw new IllegalArgumentException("토큰에 userId가 존재하지 않습니다.");
        }

        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else {
            // 문자열 등 다른 타입으로 들어올 경우를 대비한 처리
            return Long.parseLong(userIdObj.toString());
        }
    }
    
    private HotelDto toDto(Hotel hotel) {
        HotelDto dto = new HotelDto();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setAddress(hotel.getAddress());
        dto.setStarRating(hotel.getStarRating());
        dto.setDescription(hotel.getDescription());
        dto.setCountry(hotel.getCountry());
        dto.setStatus(hotel.getStatus().name());

        if (hotel.getImages() != null) {
            dto.setImageUrls(hotel.getImages().stream()
                .map(image -> image.getUrl())
                .collect(Collectors.toList()));
        }

        if (hotel.getHotelAmenities() != null) {
            dto.setAmenityIds(hotel.getHotelAmenities().stream()
                .map(ha -> ha.getAmenity().getId())
                .collect(Collectors.toList()));
        }

        return dto;
    }
}