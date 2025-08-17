package com.gangnam.coupon.domain;

public final class CouponIssuedFactory {
    /**
     * 유저에게 발급된 쿠폰 엔티티 생성
     */
    private CouponIssuedFactory() {}
    public static CouponIssued create(CouponInventory coupon, Long userId) {
       return new CouponIssued(coupon,userId);
    }
}
