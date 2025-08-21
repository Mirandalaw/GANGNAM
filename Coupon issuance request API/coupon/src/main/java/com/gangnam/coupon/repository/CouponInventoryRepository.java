package com.gangnam.coupon.repository;

import com.gangnam.coupon.domain.CouponInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 쿠폰 재고 ( coupon_inventory) Repository
 *
 * - 조건부 감소 ( Conditional Decrement ) : stcok > 0 일 때만 원자적으로 1 감소
 * - 초기 데이터 UPSERT : PostgerSQL ON CONFLICT 를 사용해 멱등하게 삽입
 */
public interface CouponInventoryRepository extends JpaRepository<CouponInventory, Long> {
    // 고유 코드로 쿠폰 재고 엔티티를 조회
    Optional<CouponInventory> findByCode(String code);

    /**
     * 조건부 감소 ( Conditional Decrement )
     * stock > 0 인 경우만 stock = stock - 1 을 수행.
     * 반환값 ( Affected Rows ) 로 성공/실패 판단
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CouponInventory c " +
            "SET c.stock = c.stock - 1 " +
            "WHERE c.id = :id AND c.stock > 0")
    int decrementIfInStock(@Param("id") Long id);

    /**
     * 초기 재고 데이터 UPSERT
     *  INSERT ... ON CONFLICT (code) DO NOTHING
     *  - 존재하지 않으면 새로 삽입
     *  - 이미 존재( UNIQUE 충동 시 ) 하면 아무 작업 x
     */
    @Modifying
    @Query(value = """
                    INSERT INTO coupon_inventory(code, stock)
                    VALUES (:code, :initialStock)
                    ON CONFLICT (code) DO NOTHING
            """, nativeQuery = true)
    int insertIgnoreConflict(@Param("code") String code, @Param("initialStock") int initialStock);
}
