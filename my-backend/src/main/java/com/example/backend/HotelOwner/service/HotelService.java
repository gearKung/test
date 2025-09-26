package com.example.backend.HotelOwner.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.HotelOwner.domain.Amenity;
import com.example.backend.HotelOwner.domain.Hotel;
import com.example.backend.HotelOwner.domain.HotelAmenity;
import com.example.backend.HotelOwner.domain.HotelImage;
import com.example.backend.HotelOwner.domain.Room;
import com.example.backend.HotelOwner.domain.User;
import com.example.backend.HotelOwner.dto.DailySalesDto;
import com.example.backend.HotelOwner.dto.DashboardDto;
import com.example.backend.HotelOwner.dto.HotelDto;
import com.example.backend.HotelOwner.dto.RoomDto;
import com.example.backend.HotelOwner.dto.SalesChartRequestDto;
import com.example.backend.HotelOwner.repository.AmenityRepository;
import com.example.backend.HotelOwner.repository.HotelAmenityRepository;
import com.example.backend.HotelOwner.repository.HotelRepository;
import com.example.backend.HotelOwner.repository.RoomRepository;
import com.example.backend.hotel_reservation.domain.Reservation;
import com.example.backend.hotel_reservation.dto.ReservationDtos;
import com.example.backend.hotel_reservation.repository.ReservationRepository;
import com.example.backend.payment.repository.PaymentRepository;
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
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

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

    // 호텔 소유주 ID로 모든 예약을 조회하는 서비스 메서드
    @Transactional(readOnly = true)
    public List<ReservationDtos.OwnerReservationResponse> getReservationsByOwner(Long ownerId) {
        // 1. 소유주의 호텔에 해당하는 모든 예약 조회
        List<Reservation> reservations = reservationRepository.findAllByHotelOwnerId(ownerId);
        if (reservations.isEmpty()) {
            return List.of(); // 예약이 없으면 빈 리스트 반환
        }

        // 2. N+1 문제 해결을 위해 필요한 ID들을 한 번에 추출
        List<Long> userIds = reservations.stream().map(Reservation::getUserId).distinct().collect(Collectors.toList());
        List<Long> roomIds = reservations.stream().map(Reservation::getRoomId).distinct().collect(Collectors.toList());

        // 3. ID 목록으로 필요한 엔티티들을 한 번의 쿼리로 조회하여 Map으로 변환
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, user -> user));
        Map<Long, Room> roomMap = roomRepository.findAllById(roomIds).stream().collect(Collectors.toMap(Room::getId, room -> room));

        // 4. 조회된 엔티티들을 이용해 DTO 조립
        return reservations.stream()
                .map(reservation -> {
                    User user = userMap.get(reservation.getUserId());
                    Room room = roomMap.get(reservation.getRoomId());
                    // User 또는 Room 정보가 없을 경우를 대비한 방어 코드
                    if (user == null || room == null) {
                        return null;
                    }
                    return new ReservationDtos.OwnerReservationResponse(reservation, user, room);
                })
                .filter(dto -> dto != null) // null인 DTO는 제외
                .collect(Collectors.toList());
    }


    // 대시보드 관련
    public DashboardDto getSalesSummary(Long ownerId) {
        // LocalDate today = LocalDate.now();
        LocalDate today = LocalDate.of(2025, 10, 5);
        LocalDate yesterday = today.minusDays(1);

        // 오늘 & 어제
        long todaySales = getSalesForDate(ownerId, today);
        long yesterdaySales = getSalesForDate(ownerId, yesterday);

        // 이번 주 & 지난 주 (월요일 ~ 일요일 기준)
        LocalDate thisWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate thisWeekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        long thisWeekSales = getSalesForDateRange(ownerId, thisWeekStart, thisWeekEnd);

        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekEnd.minusWeeks(1);
        long lastWeekSales = getSalesForDateRange(ownerId, lastWeekStart, lastWeekEnd);
        
        // 이번 달 & 지난 달
        LocalDate thisMonthStart = today.withDayOfMonth(1);
        LocalDate thisMonthEnd = today.with(TemporalAdjusters.lastDayOfMonth());
        long thisMonthSales = getSalesForDateRange(ownerId, thisMonthStart, thisMonthEnd);

        LocalDate lastMonth = today.minusMonths(1);
        LocalDate lastMonthStart = lastMonth.withDayOfMonth(1);
        LocalDate lastMonthEnd = lastMonth.with(TemporalAdjusters.lastDayOfMonth());
        long lastMonthSales = getSalesForDateRange(ownerId, lastMonthStart, lastMonthEnd);

        return DashboardDto.builder()
                .todaySales(todaySales)
                .thisWeekSales(thisWeekSales)
                .thisMonthSales(thisMonthSales)
                .salesChangeVsYesterday(calculateChangePercentage(todaySales, yesterdaySales))
                .salesChangeVsLastWeek(calculateChangePercentage(thisWeekSales, lastWeekSales))
                .salesChangeVsLastMonth(calculateChangePercentage(thisMonthSales, lastMonthSales))
                .build();
    }

    private long getSalesForDate(Long ownerId, LocalDate date) {
        Instant start = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return paymentRepository.sumCompletedPaymentsByOwnerAndDateRange(ownerId, start, end);
    }

    private long getSalesForDateRange(Long ownerId, LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return paymentRepository.sumCompletedPaymentsByOwnerAndDateRange(ownerId, start, end);
    }

    private double calculateChangePercentage(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0; // 이전 매출이 0이면, 현재 매출이 있으면 100% 증가, 없으면 0%
        }
        return ((double) (current - previous) / previous) * 100;
    }

    // 일별 매출 데이터를 조회하는 서비스 메소드
    @Transactional(readOnly = true)
    public List<DailySalesDto> getDailySales(Long ownerId, SalesChartRequestDto requestDto) {
        LocalDateTime start = requestDto.getStartDate().atStartOfDay();
        LocalDateTime end = requestDto.getEndDate().plusDays(1).atStartOfDay();

        return paymentRepository.findDailySalesByOwner(
            ownerId,
            start,
            end,
            requestDto.getHotelId(),
            requestDto.getRoomType() 
        );
    }

    //  대시보드 오늘의 현황 및 최근 예약을 위한 서비스 메소드
    public ReservationDtos.DashboardActivityResponse getDashboardActivity(Long ownerId) {
        // 테스트를 위해 오늘 날짜를 2025-10-05로 고정 (실제 운영 시 LocalDate.now() 사용)
        LocalDate today = LocalDate.of(2025, 10, 5);
        // LocalDate today = LocalDate.now();

        Instant startOfDay = today.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfDay = today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        // 오늘의 체크인/체크아웃 조회
        List<Reservation> checkIns = reservationRepository.findCheckInsForOwnerByDateRange(ownerId, startOfDay, endOfDay);
        List<Reservation> checkOuts = reservationRepository.findCheckOutsForOwnerByDateRange(ownerId, startOfDay, endOfDay);

        // 최근 예약 3일전까지만 조회
        LocalDateTime threeDaysAgo = LocalDateTime.now().minus(3, ChronoUnit.DAYS);

        List<Reservation> recentReservations = reservationRepository.findRecentReservationsForOwner(ownerId, threeDaysAgo, PageRequest.of(0, 5));

        List<Reservation> allReservations = Stream.of(checkIns, checkOuts, recentReservations)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        if (allReservations.isEmpty()) {
            return new ReservationDtos.DashboardActivityResponse(List.of(), List.of(), List.of());
        }

        List<Long> userIds = allReservations.stream().map(Reservation::getUserId).distinct().collect(Collectors.toList());
        List<Long> roomIds = allReservations.stream().map(Reservation::getRoomId).distinct().collect(Collectors.toList());

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, user -> user));
        Map<Long, Room> roomMap = roomRepository.findAllById(roomIds).stream().collect(Collectors.toMap(Room::getId, room -> room));

        Function<Reservation, ReservationDtos.OwnerReservationResponse> toDto = reservation -> {
            User user = userMap.get(reservation.getUserId());
            Room room = roomMap.get(reservation.getRoomId());
            if (user == null || room == null) return null;
            // 위에서 추가한 fromEntity 정적 메서드를 사용합니다.
            return ReservationDtos.OwnerReservationResponse.fromEntity(reservation, user, room);
        };

        List<ReservationDtos.OwnerReservationResponse> checkInDtos = checkIns.stream()
            .map(toDto)
            .filter(dto -> dto != null)
            .collect(Collectors.toList());

        List<ReservationDtos.OwnerReservationResponse> checkOutDtos = checkOuts.stream()
            .map(toDto)
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
            
        List<ReservationDtos.OwnerReservationResponse> recentReservationDtos = recentReservations.stream()
            .map(toDto)
            .filter(dto -> dto != null)
            .collect(Collectors.toList());

        return new ReservationDtos.DashboardActivityResponse(checkInDtos, checkOutDtos, recentReservationDtos);
    }
}
