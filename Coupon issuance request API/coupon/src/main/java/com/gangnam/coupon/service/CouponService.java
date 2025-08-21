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
 * CouponService
 * - 쿠폰 발급 비즈니스 로직을 담당하는 서비스 계층.
 *
 *  동시성 제어 및 유저 중복 발급 방지
 *  - 발급 방식 : A -> B -> C 순서로 시도 ( 단순 )
 */
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponInventoryRepository inventoryRepository;
    private final CouponIssuedRepository issuedRepository;
    private final EntityManager em;

    // 비즈니스용 예외 : 모든 재고 소진
    public static class SoldOutException extends RuntimeException {
        public SoldOutException(String message) { super(message); }
    }

    // 중복 발급 시도
    public static class AlreadyIssuedException extends RuntimeException {
        public AlreadyIssuedException(String message) { super(message); }
    }

    /**
     * 유저에게 쿠폰 1개 발급(유저당 최대 1회).
     * 우선순위: A -> B -> C
     *
     * @param userId 발급 대상 유저 ID
     * @return 발급 성공 시 생성된 CouponIssued ID
     *
     * @throws AlreadyIssuedException 이미 발급받은 유저일 경우
     * @throws SoldOutException 모든 쿠폰이 품절된 경우
     */
    @Transactional
    public Long issueOneForUser(Long userId) {
        // 1) 빠른 중복 방지(소프트 체크). 최종 방지는 UNIQUE(user_id)로 이중 방어.
        issuedRepository.findByUserId(userId).ifPresent(it -> {
            throw new AlreadyIssuedException("이미 발급된 유저입니다. userId=" + userId);
        });

        // 2) 발급 순서 정책 ( 필요 시 DI로 교체 )
        List<String> priority = List.of("A", "B", "C");

        // 3) 각 코드별 재고조회 없으면 404
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
                    // UNIQUE(user_id) 위반 → 비즈니스 예외로 변환
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
