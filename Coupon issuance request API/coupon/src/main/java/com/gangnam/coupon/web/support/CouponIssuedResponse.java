package com.gangnam.coupon.web.support;

import java.time.OffsetDateTime;

/**
 * CouponIssuedResponse
 *  - REST API 응답 전용 DTO
 *  - 엔티티( CouponIssued ) 를 직접 노출하지 않고,
 *    필요한 필드만 클라이언트에 전달하기 위해 도입
 * @param id 발급 행 ID
 * @param code 발급된 쿠폰 코드
 * @param userId 쿠폰을 받은 유저 ID
 * @param issuedAt 발급 시간
 */
public record CouponIssuedResponse(
        Long id,
        String code,
        Long userId,
        OffsetDateTime issuedAt
) {
}
