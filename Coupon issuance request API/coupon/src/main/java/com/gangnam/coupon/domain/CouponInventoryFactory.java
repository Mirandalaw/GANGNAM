package com.gangnam.coupon.domain;

public class CouponInventoryFactory {
    /**
     * 쿠폰 재고 엔티티 생성
     */
    private CouponInventoryFactory() {}

    public static CouponInventory create(String code, int initailStock) {
        return new CouponInventory(code, initailStock);
    }
}
