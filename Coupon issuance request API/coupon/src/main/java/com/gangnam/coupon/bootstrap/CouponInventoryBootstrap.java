package com.gangnam.coupon.bootstrap;

import com.gangnam.coupon.repository.CouponInventoryRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        value ="coupon.bootstrap.enabled",
        havingValue = "true",
        matchIfMissing = true
)

/**
 * 서버 가동 시 초기 재고를 삽입하는 부트스트랩 컴포넌트
 * - 목적
 *  A = 1, B = 30, C = 69 총 3개 Code의 초기 재고 생성
 *  테스트 환경에서 coupon.bootstrap.enabled로 on/off
 */
public class CouponInventoryBootstrap implements ApplicationRunner {

    private final CouponInventoryRepository inventoryRepository;

    // 초기 재고
    private static final List<CouponSeed> CouponSeedS = List.of(
            new CouponSeed("A", 1),
            new CouponSeed("B", 30),
            new CouponSeed("C", 69)
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 각 코드에 대해 " 존재하지 않으면 삽입, 있으면 패스 "
        for (CouponSeed s : CouponSeedS) {
            int affected = inventoryRepository.insertIgnoreConflict(s.code(), s.stock());
            if (affected == 1) {
                // 새로 삽입된 경우 : 최초 생성
                log.info("[BOOTSTRAP] 쿠폰 시드 만들기 성공. {}: {}", s.code(), s.stock());
            } else {
                // 이미 존재하는 경우 : 중복 삽입 x
                log.info("[BOOTSTRAP] 쿠폰 시드가 있슴다. : {}", s.code());
            }
        }
    }

    /**
     * DTO ( record )
     * @param code 쿠폰 코드( UNIQUE )
     * @param stock 초기 재고 수량 ( 0 이상 )
     */
    private record CouponSeed(String code, int stock) {}
}