package com.gangnam.coupon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "coupon_issued",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_coupon_issued_user", columnNames = "user_id") // 유저당 1회
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class CouponIssued {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private CouponInventory coupon; // 어떤 쿠폰을 받았는지 기록

    @Column(name = "user_id", nullable = false,updatable = false)
    private Long userId; // 발급 대상 ( 유저당 1회 )

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private OffsetDateTime createdAt; // 발급 시각
}
