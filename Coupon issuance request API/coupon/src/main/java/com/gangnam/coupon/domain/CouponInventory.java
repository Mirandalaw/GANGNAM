package com.gangnam.coupon.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "coupon_inventory")
@Getter
public class CouponInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 16, unique = true)
    private String code;

    @Column(nullable = false)
    private Integer initialStock;
}
