package com.gangnam.coupon.web;

import com.gangnam.coupon.service.CouponService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 예외 -> HTTP 상태코드 매핑
 * - SoldOutException : 409 (CONFLICT)
 * - AlreadyIssuedException : 409
 * - EntityNotFoundException : 404 (NOT_FOUND)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 품절/소진 → 409
    @ExceptionHandler(CouponService.SoldOutException.class)
    public ResponseEntity<String> handleSoldOut(CouponService.SoldOutException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    // 중복 발급 → 409
    @ExceptionHandler(CouponService.AlreadyIssuedException.class)
    public ResponseEntity<String> handleAlreadyIssued(CouponService.AlreadyIssuedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    // 엔티티 없음 → 404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    // 제약 위반(백업) → 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleConstraint(DataIntegrityViolationException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body("데이터 제약 위반");
    }
}
