package com.example.backend.review.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.backend.HotelOwner.domain.Hotel;
import com.example.backend.HotelOwner.domain.User;
import com.example.backend.hotel_reservation.domain.Reservation;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", unique = true)
    private Reservation reservation;

    // 리뷰 작성자 (유저)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 리뷰 대상 호텔
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    // 평점 (1~5)
    @Column(nullable = false)
    private int rating;

    // 리뷰 내용
    @Column(nullable = false, length = 1000)
    private String content;

    // 리뷰 이미지 여러 장
    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    // 사장님 답글 (1:1)
    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReviewReply reply;

    // // 리뷰 노출 여부 (삭제 대신 숨김 처리)
    // @Builder.Default 
    // @Column(nullable = false)
    // private boolean visible = true;

    public enum ReviewStatus {
        VISIBLE,  // 노출 중
        REPORTED, // 신고됨 (관리자 확인 대기)
        HIDDEN    // 관리자에 의해 숨김 처리됨
    }

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.VISIBLE;

    // 생성 및 수정 시간
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
