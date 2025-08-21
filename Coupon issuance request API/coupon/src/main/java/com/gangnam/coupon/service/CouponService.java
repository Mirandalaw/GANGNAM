package com.gangnam.coupon.service;

import com.gangnam.coupon.domain.CouponInventory;
import com.gangnam.coupon.domain.CouponIssued;
import com.gangnam.coupon.domain.CouponIssuedFactory;
import com.gangnam.coupon.repository.CouponInventoryRepository;
import com.gangnam.coupon.repository.CouponIssuedRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 동시성 하에서 200명이 요청해도 재고(A:1, B:30, C:69) 합계 100명만 성공하도록 보장.
 * - "조건부 감소" JPQL UPDATE로 원자적 감소 (stock > 0 인 경우에만 -1)
 * - 유저 중복 발급은 UNIQUE(user_id) 제약으로 최종 차단
 * - 발급 정책: A -> B -> C 우선순위 (단순/결정적). 필요 시 다른 전략으로 교체 가능.
 */
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponInventoryRepository inventoryRepository;
    private final CouponIssuedRepository issuedRepository;
    private final EntityManager em;

    /** 비즈니스용 예외 */
    public static class SoldOutException extends RuntimeException {
        public SoldOutException(String message) { super(message); }
    }
    public static class AlreadyIssuedException extends RuntimeException {
        public AlreadyIssuedException(String message) { super(message); }
    }

    /**
     * 유저에게 쿠폰 1개 발급(유저당 최대 1회).
     * 우선순위: A -> B -> C
     */
    @Transactional
    public Long issueOneForUser(Long userId) {
        // 1) 빠른 중복 방지(소프트 체크). 최종 방지는 UNIQUE(user_id)로 이중 방어.
        issuedRepository.findByUserId(userId).ifPresent(it -> {
            throw new AlreadyIssuedException("이미 발급된 유저입니다. userId=" + userId);
        });

        // 2) 발급 시도 순서(정책). 필요 시 주입/설정으로 교체 가능.
        List<String> priority = List.of("A", "B", "C");

        // 3) 각 코드의 ID를 미리 확보(없으면 404)
        List<CouponInventory> inventories = priority.stream()
                .map(code -> inventoryRepository.findByCode(code)
                        .orElseThrow(() -> new EntityNotFoundException("쿠폰 코드가 존재하지 않습니다: " + code)))
                .toList();

        // 4) 순서대로 조건부 감소 시도
        for (CouponInventory inv : inventories) {
            int updated = inventoryRepository.decrementIfInStock(inv.getId());
            if (updated == 1) {
                // 감소 성공 → 발급 레코드 저장
                // getReference로 불필요한 SELECT 없이 FK만 연결
                CouponInventory ref = em.getReference(CouponInventory.class, inv.getId());
                CouponIssued issued = CouponIssuedFactory.create(ref, userId);
                try {
                    issuedRepository.saveAndFlush(issued);
                } catch (DataIntegrityViolationException e) {
                    // 동시 중복 요청 etc. UNIQUE(user_id) 위반 → 비즈니스 예외로 변환
                    throw new AlreadyIssuedException("이미 발급된 유저입니다. userId=" + userId);
                }
                return issued.getId();
            }
            // updated == 0 : 품절 → 다음 코드로 넘어감
        }

        // 5) 전부 실패면 품절
        throw new SoldOutException("모든 쿠폰 재고가 소진되었습니다.");
    }
}
