package com.gangnam.coupon.domain;

/**
 * CouponIssuedFactory
 * - CouponIssued 엔티티 생성을 담당하는 팩토리 클래스
 *
 * - 도메인 객체 ( CouponIssued ) 는 new로 직접 생성 x
 *   Factory 를 통해서만 생성되도록 제한
 *  - 생성자 내부 유효성 검사
 */
public final class CouponIssuedFactory {

    /**
     * private 생성자
     * - 인스턴스화 방지 ( 정적 팩토리 메서드만 사용하도록 )
     */
    private CouponIssuedFactory() {}

    /**
     * 쿠폰 발급 엔티티 생성
     * @param coupon 발급 대상 쿠폰 ( 재고 감소가 선행되어야 함 )
     * @param userId 발급받을 사용자 ID
     * @return CouponIssued 엔티티
     */
    public static CouponIssued create(CouponInventory coupon, Long userId) {
       return new CouponIssued(coupon,userId);
    }
}
