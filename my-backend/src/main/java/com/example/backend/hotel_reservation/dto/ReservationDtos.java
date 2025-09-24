package com.example.backend.hotel_reservation.dto;

import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import com.example.backend.HotelOwner.domain.Room;
import com.example.backend.HotelOwner.domain.User;
import com.example.backend.hotel_reservation.domain.Reservation;
import com.example.backend.hotel_reservation.domain.ReservationStatus;

public class ReservationDtos {

    @Getter @Setter
    public static class HoldRequest {
        private Long userId;
        private Long roomId;
        private Integer qty;          // 예약 객실 수
        private String checkIn;       // 'YYYY-MM-DD'
        private String checkOut;      // 'YYYY-MM-DD'
        private Integer adults;       // optional
        private Integer children;     // optional
        private Integer holdSeconds;  // optional, default 30
    }

    @Getter @Builder
    public static class HoldResponse {
        private Long reservationId;
        private Instant expiresAt;
        private String status; // PENDING
    }

    @Getter @Builder
    public static class ReservationDetail {
        private Long id;
        private String status;
        private Instant expiresAt;
        private Long hotelId; 
        private Long userId;
        private Long roomId;
        private Integer numRooms;
        private Integer adults;
        private Integer children;
        private Instant startDate;
        private Instant endDate;
    }

    @Getter
    @NoArgsConstructor
    public static class OwnerReservationResponse {
        private Long id;
        private String guestName;
        private String guestPhone;
        private String hotelName;
        private String roomType;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private int adults;
        private int children;
        private ReservationStatus status;
        private long nights;

        public OwnerReservationResponse(Reservation reservation, User user, Room room) {
            this.id = reservation.getId();
            this.guestName = user.getName(); // 예약자(손님) 이름
            this.guestPhone = user.getPhone();
            this.hotelName = room.getHotel().getName();
            this.roomType = room.getRoomType().name();
            // Instant를 LocalDate로 변환
            this.checkInDate = reservation.getStartDate().atZone(ZoneId.systemDefault()).toLocalDate();
            this.checkOutDate = reservation.getEndDate().atZone(ZoneId.systemDefault()).toLocalDate();
            this.adults = reservation.getNumAdult();
            this.children = reservation.getNumKid();
            this.status = reservation.getStatus();
            this.nights = ChronoUnit.DAYS.between(this.checkInDate, this.checkOutDate);
        }
    }
}
