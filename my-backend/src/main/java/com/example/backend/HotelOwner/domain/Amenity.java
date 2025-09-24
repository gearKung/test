package com.example.backend.HotelOwner.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "amenity")
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false)
    private FeeType feeType = FeeType.FREE;

    @Column(name = "fee_amount")
    private Integer feeAmount;

    @Column(name = "fee_unit", length = 50)
    private String feeUnit;

    @Column(name = "operating_hours", length = 255)
    private String operatingHours;

    @Column(length = 255)
    private String location;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category = Category.OTHER;
    
    // HotelAmenity와의 관계. Amenity가 삭제될 때 연결된 정보도 함께 삭제됩니다.
    @OneToMany(mappedBy = "amenity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HotelAmenity> hotelAmenities = new ArrayList<>();


    public enum FeeType {
        FREE, PAID, HOURLY
    }

    public enum Category {
        IN_ROOM, IN_HOTEL, LEISURE, FNB, BUSINESS, OTHER
    }
}