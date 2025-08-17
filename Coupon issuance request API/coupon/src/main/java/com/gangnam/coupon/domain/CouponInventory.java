package com.gangnam.coupon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupon_inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 16, unique = true)
    private String code;

    @Column(nullable = false)
    private Integer initialStock;

    CouponInventory(String code, Integer initialStock) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
        if (initialStock == null || initialStock < 0) {
            throw new IllegalArgumentException("initialStock must be >=0");
        }
    }
}
