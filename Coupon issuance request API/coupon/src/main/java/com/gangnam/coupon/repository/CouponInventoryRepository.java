package com.gangnam.coupon.repository;

import com.gangnam.coupon.domain.CouponInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponInventoryRepository extends JpaRepository<CouponInventory,Long> {
    Optional<CouponInventory> findByCode(String code);
}
