package com.gangnam.coupon.service;

import com.gangnam.coupon.domain.CouponInventory;
import com.gangnam.coupon.domain.CouponIssued;
import com.gangnam.coupon.domain.CouponIssuedFactory;
import com.gangnam.coupon.repository.CouponInventoryRepository;
import com.gangnam.coupon.repository.CouponIssuedRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CouponService
 * - 쿠폰 발급 비즈니스 로직을 담당하는 서비스 계층.
 * 1. 유저가 발급 받은 적이 있는지 확인 -> throw AlreadyIssuedException
 * 2. List 에 ["A","B","C"]  넣고 재고 차감
 *   1) 쿠폰이 있는지 확인 - code로
 *   2) 조건부 감소 decrementIfInStock - 없으면 쿠폰 코드 x
 *   3) issue 엔티티 생성 -> save
 * 3. 모두 실패 시 품절
 */
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponInventoryRepository inventoryRepository;
    private final CouponIssuedRepository issuedRepository;

    @Transactional
    public Long issueCoupon(Long userId) {

        // 1. 유저가 발급 받은 적이 있는지 확인 -> throw AlreadyIssuedException
        if (issuedRepository.existsByUserId(userId)) {
            throw new AlreadyIssuedException("이미 발급된 유저임");
        }

        // List 에 ["A","B","C"]  넣고 재고 차감
        List<String> coupon = List.of("A", "B", "C");

        for (String code : coupon) {
            // 2-1. 쿠폰이 있는지 확인 - code로
            Optional<CouponInventory> inventory = inventoryRepository.findByCode(code);
            if (inventory.isEmpty()) {
                throw new EntityNotFoundException(" 쿠폰 코드가 존재 x ");
            }

            // 2-2. 조건부 감소 decrementIfInStock - 없으면 쿠폰 코드 x
            int updated = inventoryRepository.decrementIfInStock(inventory.get().getId());
            if (updated == 0) continue; // 재고 없음.

            // 2-3. issue 엔티티 생성 -> save
            try {
                CouponIssued issued = CouponIssuedFactory.create(inventory.get(), userId);
                CouponIssued save = issuedRepository.saveAndFlush(issued);
                return save.getId();
            } catch (DataIntegrityViolationException e) {
                throw new AlreadyIssuedException(" 이미 발급된 유저임 ");
            }
        }
        throw new SoldOutException("모든 쿠폰 재고가 소진되었습니다.");
    }
}
