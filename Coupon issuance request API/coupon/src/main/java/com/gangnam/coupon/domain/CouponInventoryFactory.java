package com.gangnam.coupon.domain;

/**
 * CouponInventoryFactory
 * - CouponInventory 엔티티의 생성 책임을 전담하는 팩토리 클래스
 *  new CouponInventory(...) 직접 호출 x
 */
public final class CouponInventoryFactory {

    /**
     * private 생성자
     * - 외부에서 new CouponInventoryFactory() 호출하지 못하도록 차단
     * - 순수 유틸성 클래스임을 보장
     */
    private CouponInventoryFactory() {
    }

    /**
     * CoupoonInveotry 생성 매소드
     * - 도메인 객체의 일관성을 보장하기 위해 팩토리를 통해서만 생성
     * @param code 쿠폰 코드
     * @param initialStock 초기 재고 수량
     * @return CouponInventory 엔티티 인스턴스
     */
    public static CouponInventory create(String code, int initialStock) {
        return new CouponInventory(code, initialStock);
    }
}
