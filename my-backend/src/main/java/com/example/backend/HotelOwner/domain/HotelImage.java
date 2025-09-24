package com.example.backend.HotelOwner.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hotel_image")
public class HotelImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    @JsonIgnore
    private Hotel hotel;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String url;

    @Column(name = "sort_no", nullable = false)
    private int sortNo = 0;

    @Column(name = "is_cover", nullable = false)
    private boolean isCover = false;

    @Column(length = 255)
    private String caption;

    @Column(name = "alt_text", length = 255)
    private String altText;
}