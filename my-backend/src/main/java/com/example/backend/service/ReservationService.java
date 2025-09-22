package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.backend.domain.Reservation;
import com.example.backend.domain.Room;
import com.example.backend.domain.User;
import com.example.backend.repository.ReservationRepository;
import com.example.backend.repository.RoomRepository;
import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    // 예약 생성
    public Reservation createReservation(Long userId, Long roomId, Reservation reservation) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("객실을 찾을 수 없습니다."));

        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setStatus(Reservation.Status.PENDING);

        return reservationRepository.save(reservation);
    }

    // 전체 예약 조회
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    // 사용자별 예약 조회
    public List<Reservation> getReservationsByUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    // 객실별 예약 조회
    public List<Reservation> getReservationsByRoom(Long roomId) {
        return reservationRepository.findByRoomId(roomId);
    }

    // 예약 삭제
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }
}