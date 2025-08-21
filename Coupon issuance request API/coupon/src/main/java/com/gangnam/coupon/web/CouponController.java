package com.gangnam.coupon.web;

import com.gangnam.coupon.service.CouponService;
import com.gangnam.coupon.web.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * CouponController
 * - 쿠폰 발급 API 엔드포인트
 * - userId를 Path Variable 로 수신
 * - 응답 스키마는 ApiResponse<T> 규격
 *
 * 예외 처리
 * - 비즈니스 예외는 GlobalExceptionHandler에서 상태코드 및 메시지 매핑
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
@Validated
public class CouponController {
    private final CouponService couponService;

    /**
     * 쿠폰 발급 ( 유저당 최대 1회 )
     * Request
     * - Path Variable : userId ( 양수 )
     */
    @PostMapping("/issue/{userId}")
    public ResponseEntity<ApiResponse<Long>> issueCoupon(
            @PathVariable("userId") Long userId
    ) {
        Long issuedId = couponService.issueOneForUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(issuedId));
    }
}
