package com.gangnam.coupon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * CouponInventory
 * - 특정 쿠폰 코드 ( code ) 에 대한 현재 보유 재고 ( stock ) 을 관리하는 엔티티
 *  1. code는 고유 ( unique )
 *  2. stock 은 0 이상의 정수
 */
@Entity
@Table(name = "coupon_inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 외부 직접 호출 방지용 어노테이션
public class CouponInventory {
    // DB 자동 증가 (IDENTITY 전략 사용)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키

    // 동일 코드 중복 생성 방지를 위해 : 유니크 제약조건
    @Column(nullable = false, length = 16, unique = true)
    private String code; // 쿠폰 코드 ( 유니크 )

    // >=0
    @Column(nullable = false)
    private int stock; // 남은 재고 > = 0

    /**
     * 생성자 ( 팩토리 전용 )
     * - 외부에서 직접 new 하지 못하도록 package-private
     * - 불변 ( code - not null/blan, initialStock >=0 )
     * @param code 쿠폰 코드
     * @param initialStock 초기 재고 수량
     */
    CouponInventory(String code, Integer initialStock) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("쿠폰 코드가 필요함.");
        }
        if (initialStock == null || initialStock < 0) {
            throw new IllegalArgumentException("초기 수량은 >=0");
        }
        this.code = code.trim();
        this.stock = initialStock;
    }
}
