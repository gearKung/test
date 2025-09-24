package com.example.backend.HotelOwner.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(exclude = {"owner", "rooms", "images", "hotelAmenities"}) 
@Table(name="hotel")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 호텔 소유자 (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // 🔥 owner를 응답에서 제외
    private User owner;

    @Column(name = "business_id", nullable = true) //나중에 비지니스 테이블 만들면 그냥 쓰기
    private Long businessId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "star_rating", nullable = false)
    private Integer starRating;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String image;

    @Column(nullable = false, length = 50)
    private String country;

    // @Enumerated(EnumType.STRING)
    // @Column(nullable = false, length = 20)
    // private Status status = Status.PENDING; // 기본 승인대기
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.APPROVED; // 기본은 승인 완료

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HotelAmenity> hotelAmenities = new ArrayList<>();
    
    // HotelImage 와의 관계는 이전과 동일하게 유지
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HotelImage> images = new ArrayList<>();

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
