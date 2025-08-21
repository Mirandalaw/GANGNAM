package com.gangnam.coupon;

import com.gangnam.coupon.domain.CouponInventory;
import com.gangnam.coupon.domain.CouponInventoryFactory;
import com.gangnam.coupon.repository.CouponInventoryRepository;
import com.gangnam.coupon.repository.CouponIssuedRepository;
import com.gangnam.coupon.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@SpringBootTest
class CouponApplicationTests {

    @Autowired CouponService couponService;
    @Autowired CouponInventoryRepository inventoryRepository;
    @Autowired CouponIssuedRepository issuedRepository;

    @BeforeEach
    void setUp() {
        issuedRepository.deleteAllInBatch();
        inventoryRepository.deleteAllInBatch();

        // 요구 시나리오: A=1, B=30, C=69 (총 100)
        List<CouponInventory> seeds = new ArrayList<>();
        seeds.add(CouponInventoryFactory.create("A", 1));
        seeds.add(CouponInventoryFactory.create("B", 30));
        seeds.add(CouponInventoryFactory.create("C", 69));
        inventoryRepository.saveAllAndFlush(seeds);
    }

    @AfterEach
    void tearDown() {
        issuedRepository.deleteAllInBatch();
        inventoryRepository.deleteAllInBatch();
    }

    @Test
    void issued_concurrently_200_users_expect_A1_B30_C69_total_100() throws InterruptedException {
        final int totalUsers = 200;
        ExecutorService pool = Executors.newFixedThreadPool(totalUsers);

        CountDownLatch ready = new CountDownLatch(totalUsers);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(totalUsers);

        AtomicInteger success   = new AtomicInteger(0);
        AtomicInteger soldOut   = new AtomicInteger(0);
        AtomicInteger duplicate = new AtomicInteger(0);

        for (int i = 1; i <= totalUsers; i++) {
            final long userId = i; // 각 요청은 서로 다른 유저
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await(); // 동시에 출발
                    couponService.issueOneForUser(userId);
                    success.incrementAndGet();
                } catch (CouponService.SoldOutException e) {
                    soldOut.incrementAndGet();
                } catch (CouponService.AlreadyIssuedException e) {
                    duplicate.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    done.countDown();
                }
            });
        }

        // 동시 발사
        ready.await(10, TimeUnit.SECONDS);
        start.countDown();

        // 완료 대기
        boolean finished = done.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertThat(finished).as("모든 작업이 시간 내 완료되어야 함").isTrue();

        // 1) 총 발급 100건
        long issuedCount = issuedRepository.count();
        assertThat(success.get()).as("발급 성공 수는 정확히 100건").isEqualTo(100);
        assertThat(issuedCount).as("DB에 기록된 발급 수 역시 100건").isEqualTo(100);
        assertThat(soldOut.get() + success.get() + duplicate.get())
                .as("전체 요청 수와 일치해야 함")
                .isEqualTo(totalUsers);

        // 2) 각 코드 재고 0 확인 (A1, B30, C69 → 전부 소진)
        CouponInventory a = inventoryRepository.findByCode("A").orElseThrow();
        CouponInventory b = inventoryRepository.findByCode("B").orElseThrow();
        CouponInventory c = inventoryRepository.findByCode("C").orElseThrow();
        assertThat(a.getStock()).as("A는 1개 → 0개로 소진되어야 함").isZero();
        assertThat(b.getStock()).as("B는 30개 → 0개").isZero();
        assertThat(c.getStock()).as("C는 69개 → 0개").isZero();

        // 3) 발행 내역 상세 (상위 10건 샘플) — 인터페이스 프로젝션 사용
        List<CouponIssuedRepository.IssuedRow> issuedRows = issuedRepository.findAllIssuedView();
        log.info("[ISSUED] total={} (최신순 상위 10건)", issuedRows.size());
        issuedRows.stream().limit(10).forEach(r ->
                log.info(" - id={}, code={}, userId={}, createdAt={}",
                        r.getId(), r.getCouponCode(), r.getUserId(), r.getCreatedAt())
        );
        assertThat(issuedRows.size()).as("총 발급 건수").isEqualTo(100);
        assertThat(issuedRows.stream().allMatch(r -> r.getCreatedAt() != null))
                .as("모든 발급은 createdAt이 있어야 함").isTrue();

        // 4) 코드별 분포 집계 및 검증 (A=1, B=30, C=69)
        Map<String, Long> distribution = issuedRepository.countGroupByCode().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        log.info("[RESULT] success={}, soldOut={}, duplicate={}, issuedCountDB={}, dist={}",
                success.get(), soldOut.get(), duplicate.get(), issuedCount, distribution);

        assertThat(distribution.getOrDefault("A", 0L))
                .withFailMessage("A 분포가 1이 아님. dist=%s", distribution)
                .isEqualTo(1L);
        assertThat(distribution.getOrDefault("B", 0L))
                .withFailMessage("B 분포가 30이 아님. dist=%s", distribution)
                .isEqualTo(30L);
        assertThat(distribution.getOrDefault("C", 0L))
                .withFailMessage("C 분포가 69이 아님. dist=%s", distribution)
                .isEqualTo(69L);
    }
}