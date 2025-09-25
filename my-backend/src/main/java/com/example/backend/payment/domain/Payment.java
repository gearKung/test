package com.example.backend.payment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import com.nimbusds.jose.crypto.impl.PRFParams;

import java.time.LocalDateTime;

@Entity
@Table(name = "Payment") // SQL 테이블명과 일치
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "base_price", nullable = false)
    private int basePrice;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @ColumnDefault("0")
    @Column(nullable = false)
    private int tax;

    @ColumnDefault("0")
    @Column(nullable = false)
    private int discount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @CreationTimestamp // JPA가 엔티티를 생성할 때 현재 시간을 자동으로 기록
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "receipt_url", nullable = true, columnDefinition = "TEXT")
    private String receiptUrl;

    @Column(name = "payment_key", nullable = true, length = 100)
    private String paymentKey;

    // 결제 상태를 나타내는 Enum
    public enum PaymentStatus {
        COMPLETED, // 결제완료
        CANCELLED, // 취소
    }

    // JPA가 DB에 저장하기 직전에 호출되는 메소드
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = PaymentStatus.COMPLETED; // 기본 상태를 '결제완료'로 설정
        }
    }
}