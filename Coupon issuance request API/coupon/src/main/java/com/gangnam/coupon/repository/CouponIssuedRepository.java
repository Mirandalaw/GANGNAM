package com.gangnam.coupon.repository;

import com.gangnam.coupon.domain.CouponIssued;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponIssuedRepository extends JpaRepository<CouponIssued, Long> {
    // 특정 유저가 이미 발급 받았는지 확인
    Optional<CouponIssued> findByUserId(Long userId);


}
