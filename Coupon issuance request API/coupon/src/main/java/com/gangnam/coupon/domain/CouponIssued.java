package com.gangnam.coupon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * CouponIssued
 * - 사용자에게 쿠폰이 언제 발급되었는지 기록하는 엔티티
 * - 규칙
 *  1) 유저당 1회만 발급 가능 ( UNIQUE(user_id) )
 *  2) 발급 후 수정 불가 ( coupon, userId, createdAt 모두 updatable = false )
 *  3) 쿠폰 재고 차감은 Service 계층에서 조건부 감소 쿼리로 수행
 *
 *  - @ManyToOne(fetch = LAZY) : 발급 내역 조회 시 쿠폰 본문을 즉시 로딩하지 않음
 */
@Entity
@Table(name = "coupon_issued",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_coupon_issued_user", columnNames = "user_id") // 유저당 1회
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class CouponIssued {
    // DB 자동 증가 (IDENTITY 전략 사용)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    /**
     * 어떤 쿠폰이 발급되었는지 ( FK )
     * - LAZY 로딩 : 필요한 시점까지 지연 로딩
     * - updatable = false : 변경 금지 ( 발급 이력의 불변성 )
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false, updatable = false)
    private CouponInventory coupon; // 어떤 쿠폰을 발급했는지?

    // 유저당 1회 발급
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId; // 발급 대상 ( 유저당 1회 )

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private OffsetDateTime createdAt; // 발급 시각 - 서버 DB 기준

    /**
     * 팩토리 전용 생성자 ( 외부 new 금지 )
     * - 외부에서 직접 new 하지 못하도록 package-private
     * - 도메인 불변식 : coupon != null , userId != null
     * @param coupon CouponInventory
     * @param userId 유저 ID
     */
    CouponIssued(CouponInventory coupon, Long userId) {
        if (coupon == null) throw new IllegalArgumentException("coupon required");
        if (userId == null) throw new IllegalArgumentException("userId required");
        this.coupon = coupon;
        this.userId = userId;
    }
}
