package com.gangnam.coupon.repository;

import com.gangnam.coupon.domain.CouponIssued;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CouponIssuedRespository
 *
 * - 발급 이력 ( CouponIssued ) 조회/집계
 */
public interface CouponIssuedRepository extends JpaRepository<CouponIssued, Long> {
    // 특정 유저가 이미 발급 받았는지 여부 확인.
    Optional<CouponIssued> findByUserId(Long userId);

    // 존재 여부 판단
    boolean existsByUserId(Long userId);
    /**
     * 코드별 발급 건수 집계
     * 반환
     * - List<Object[]>
     *    * [0] = String code
     *    * [1] = Long   cnt
     *
     *  단순 집계라 엔티티 로딩 없이 수행.
     */
    @Query("""
                select i.coupon.code as code, count(i) as cnt
                        from CouponIssued i
                        group by i.coupon.code
            """)
    List<Object[]> countGroupByCode();

    /**
     * 발행 내역 상세 " 뷰 "
     * 목적
     * - 테스트 / 관리 API 에서 LAZY 초기화 문제 없이 행 단위로 내역을 확인
     * - 필요한 컬럼만 즉시 조회 ( 인터페이스 프로젝션 ) => 불필요한 엔티티 하이드레이션 회피.
     *
     * - 최신 발급 순 ( createdAt DESC )
     */
    @Query("""
            select 
                i.id as id,
                c.code as couponCode,
                i.userId as userId,
                i.createdAt as createdAt
            from CouponIssued i
            join i.coupon c
            order by i.createdAt desc
            """)
    List<IssuedRow> findAllIssuedView();


    /**
     * 인터페이스 프로젝션
     * - 쿼리의 select 별칭과 accessor 이름이 일치해야 함.
     * - Spring Data JPA 가 select 결과를 구현체 없이 동적 프록시로 매핑한다.
     */
    interface IssuedRow {
        Long getId();

        String getCouponCode();

        Long getUserId();

        OffsetDateTime getCreatedAt();
    }
}
