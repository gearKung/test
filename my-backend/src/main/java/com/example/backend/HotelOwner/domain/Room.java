package com.example.backend.HotelOwner.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(exclude = "hotel")
@Table(name="room")
@Builder
@AllArgsConstructor
@NoArgsConstructor  
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소속 호텔
    @ManyToOne(fetch = FetchType.EAGER) // LAZY -> EAGER 로 변경
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 50)
    private RoomType roomType;

    @Column(name = "room_size", nullable = false, length = 50)
    private String roomSize;

    @Column(name = "capacity_min", nullable = false)
    private Integer capacityMin;

    @Column(name = "capacity_max", nullable = false)
    private Integer capacityMax;

    @Column(name = "price", nullable = false)
    private int price; // 1박당 가격

    @Column(name = "room_count", nullable = false)
    private int roomCount; // 추가된 필드 → 같은 타입의 방이 몇 개인지

    @Column(name = "check_in_time", nullable = false)   
    private LocalTime checkInTime;

    @Column(name = "check_out_time", nullable = false)
    private LocalTime checkOutTime;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RoomImage> images = new ArrayList<>();

    public enum RoomType {
        스위트룸, 디럭스룸, 스탠다드룸, 싱글룸, 트윈룸
    }
}
