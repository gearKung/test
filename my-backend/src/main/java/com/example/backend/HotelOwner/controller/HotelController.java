package com.example.backend.HotelOwner.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.jsonwebtoken.Claims;

import com.example.backend.HotelOwner.domain.Hotel;
import com.example.backend.HotelOwner.domain.Room;
import com.example.backend.HotelOwner.dto.AmenityDto;
import com.example.backend.HotelOwner.dto.DailySalesDto;
import com.example.backend.HotelOwner.dto.DashboardDto;
import com.example.backend.HotelOwner.dto.HotelDto;
import com.example.backend.HotelOwner.dto.RoomDto;
import com.example.backend.HotelOwner.dto.SalesChartRequestDto;
import com.example.backend.HotelOwner.service.AmenityService;
import com.example.backend.HotelOwner.service.FileStorageService;
import com.example.backend.HotelOwner.service.HotelService;
import com.example.backend.HotelOwner.service.RoomService;
import com.example.backend.config.JwtUtil;
import com.example.backend.hotel_reservation.dto.ReservationDtos;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Slf4j
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
        
        log.info("1. [HotelController] /my-hotels API 호출됨");
        try {
            Long ownerId = getUserIdFromToken(authHeader);
            log.info("2. [HotelController] 토큰에서 추출된 ownerId: {}", ownerId);

            List<Hotel> hotels = hotelService.getHotelsByOwner(ownerId);
            log.info("4. [HotelController] HotelService에서 받은 호텔 수: {}", hotels.size());

            List<HotelDto> hotelDtos = hotels.stream()
                    .map(HotelDto::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("5. [HotelController] DTO로 변환된 호텔 수: {}", hotelDtos.size());
            return ResponseEntity.ok(hotelDtos);

        } catch (Exception e) {
            log.error("[HotelController] 호텔 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }   
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

        log.info("1. [Controller-수정] /api/hotels/{} API 호출됨", id);

        Long ownerId = getUserIdFromToken(authHeader);

        Hotel existingHotel = hotelService.getHotel(id);
        if (!existingHotel.getOwner().getId().equals(ownerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        
        List<String> imageUrls = new ArrayList<>();
        if (hotelDto.getImageUrls() != null) {
            imageUrls.addAll(hotelDto.getImageUrls());
        }
        if (files != null && !files.isEmpty()) {
            List<String> newImageUrls = files.stream()
                    .map(fileStorageService::store)
                    .collect(Collectors.toList());
            imageUrls.addAll(newImageUrls);
        }
                
        HotelDto updatedHotelDto = hotelService.updateHotel(id, hotelDto, imageUrls, hotelDto.getAmenityIds());
        log.info("5. [Controller-수정] 호텔 수정 완료, 호텔 ID: {}", updatedHotelDto.getId());
        
        return ResponseEntity.ok(updatedHotelDto);
    }
    // 호텔 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        log.info("1. [Controller-삭제] /api/hotels/{} API 호출됨", id);
        try {
            Long ownerId = getUserIdFromToken(authHeader);
            log.info("2. [Controller-삭제] 토큰에서 추출된 ownerId: {}", ownerId);

            Hotel existingHotel = hotelService.getHotel(id);
            if (!existingHotel.getOwner().getId().equals(ownerId)) {
                log.warn("   [Controller-삭제] 권한 없음. 호텔 소유주 ID: {}, 요청자 ID: {}", existingHotel.getOwner().getId(), ownerId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            hotelService.deleteHotel(id);
            log.info("4. [Controller-삭제] 호텔 삭제 완료, 호텔 ID: {}", id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("[Controller-삭제] 호텔 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ----- 객실관리-----

    //특정 호텔의 모든 객실 조회
    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<RoomDto>> getRoomsByHotel(@PathVariable Long hotelId) {
        log.info("1. [Controller-객실 조회] /api/hotels/{}/rooms API 호출됨", hotelId);
        try {
            // ✅ [수정] 서비스에서 바로 DTO 리스트를 받습니다.
            List<RoomDto> roomDtos = roomService.findByHotelId(hotelId);
            
            log.info("3. [Controller-객실 조회] 서비스로부터 {}개 객실 DTO 받음", roomDtos.size());
            return ResponseEntity.ok(roomDtos);
            
        } catch (Exception e) {
            log.error("[Controller-객실 조회] 객실 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 특정 호텔에 객실 등록
    @PostMapping("/{hotelId}/rooms")
    public ResponseEntity<RoomDto> createRoom(
            @PathVariable Long hotelId,
            @RequestPart("room") RoomDto roomDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("1. [Controller-객실 생성] /api/hotels/{}/rooms API 호출됨", hotelId);
        if (userDetails == null) {
            log.warn("   [Controller-객실 생성] 인증된 사용자가 아님. 401 반환");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("2. [Controller-객실 생성] 요청자: {}, 요청 DTO: {}", userDetails.getUsername(), roomDto);

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        List<String> imageUrls = new ArrayList<>();
        if (files != null && !files .isEmpty()) {
            imageUrls = files.stream()
                    .map(fileStorageService::store)
                    .collect(Collectors.toList());
        }

        RoomDto newRoomDto = roomService.createRoom(hotelId, roomDto, imageUrls, userDetails.getUsername());
    
        log.info("4. [Controller-객실 생성] 객실 생성 완료, Room ID: {}", newRoomDto.getId());
        
        // [수정] 받은 DTO를 바로 반환
        return ResponseEntity.ok(newRoomDto);
    }


    // 객실 수정
    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<RoomDto> updateRoom(
            @PathVariable Long roomId,
            @RequestPart("room") RoomDto roomDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("1. [Controller-객실 수정] /api/hotels/rooms/{} API 호출됨", roomId);
        if (userDetails == null) {
            log.warn("   [Controller-객실 수정] 인증된 사용자가 아님. 401 반환");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("2. [Controller-객실 수정] 요청자: {}, 요청 DTO: {}", userDetails.getUsername(), roomDto);
        
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
        log.info("4. [Controller-객실 수정] 객실 수정 완료, Room ID: {}", updatedRoom.getId());
        return ResponseEntity.ok(RoomDto.fromEntity(updatedRoom));
    }

    // 객실 삭제
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("1. [Controller-객실 삭제] /api/hotels/rooms/{} API 호출됨", roomId);
        if (userDetails == null) {
            log.warn("   [Controller-객실 삭제] 인증된 사용자가 아님. 401 반환");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("2. [Controller-객실 삭제] 요청자: {}", userDetails.getUsername());
        roomService.deleteRoom(roomId, userDetails.getUsername());
        log.info("4. [Controller-객실 삭제] 객실 삭제 완료, Room ID: {}", roomId);
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

    @GetMapping("/owner/{ownerId}/reservations")
    public ResponseEntity<List<ReservationDtos.OwnerReservationResponse>> getReservationsForOwner(@PathVariable Long ownerId) {
        log.info("[Controller] ownerId {}의 예약 목록 조회 API 호출", ownerId);
        List<ReservationDtos.OwnerReservationResponse> reservations = hotelService.getReservationsByOwner(ownerId);
        return ResponseEntity.ok(reservations);
    }

    // 대시보드 매출 요약 API
    @GetMapping("/dashboard/sales-summary")
    public ResponseEntity<DashboardDto> getSalesSummary(@RequestHeader("Authorization") String authHeader) {
        Long ownerId = getUserIdFromToken(authHeader);
        DashboardDto summary = hotelService.getSalesSummary(ownerId);
        return ResponseEntity.ok(summary);
    }

    // 대시보드 그래프용 일별 매출 데이터 API
    @PostMapping("/dashboard/daily-sales")
    public ResponseEntity<List<DailySalesDto>> getDailySales(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody SalesChartRequestDto requestDto) {
        Long ownerId = getUserIdFromToken(authHeader);
        List<DailySalesDto> dailySales = hotelService.getDailySales(ownerId, requestDto);
        return ResponseEntity.ok(dailySales);
    }
}