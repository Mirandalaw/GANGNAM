package com.gangnam.coupon.web.advice;

import com.gangnam.coupon.service.CouponService;
import com.gangnam.coupon.web.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

import static com.gangnam.coupon.web.support.ErrorResponses.formatViolations;
import static com.gangnam.coupon.web.support.ErrorResponses.wrap;

/**
 * 예외 -> HTTP 상태코드 + ApiResponse 매핑
 * - SoldOutException : 409 (CONFLICT)
 * - AlreadyIssuedException : 409
 * - EntityNotFoundException : 404 (NOT_FOUND)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 품절/소진 → 409
    @ExceptionHandler(CouponService.SoldOutException.class)
    public ResponseEntity<ApiResponse<Void>> handleSoldOut(CouponService.SoldOutException e) {
        return wrap(HttpStatus.CONFLICT, e.getMessage());
    }

    // 중복 발급 → 409
    @ExceptionHandler(CouponService.AlreadyIssuedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyIssued(CouponService.AlreadyIssuedException e) {
        return wrap(HttpStatus.CONFLICT, e.getMessage());
    }

    // 엔티티 없음 → 404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(EntityNotFoundException e) {
        return wrap(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 제약 위반(백업) → 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(DataIntegrityViolationException e) {
        return wrap(HttpStatus.CONFLICT, "데이터 제약 위반");
    }

    // === 요청/검증 오류 ===

    // Path 파라미터 검증 실패 -> 400
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstrainViolation(ConstraintViolationException e) {
        String msg = formatViolations(e.getConstraintViolations());
        return wrap(HttpStatus.BAD_REQUEST, msg.isBlank() ? "요청 파라미터가 유효하지 않습니다." : msg);
    }

    // 타입 불일치 ( Long 인데 문자열 ) -> 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> hanldeTypeMismatch(MethodArgumentTypeMismatchException e) {
        String name = e.getName();
        String required = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : " 올바른 타입 ";
        String msg = "요청 파라미터 '" + name + "' 타입이 잘못되었습니다. 요구 타입: " + required;
        return wrap(HttpStatus.BAD_REQUEST, msg);
    }

    // 필수 쿼리 파라미터 누락 -> 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        String msg = " 필수 파라미터가 누락되었습니다: " + e.getParameterName();
        return wrap(HttpStatus.BAD_REQUEST, msg);
    }

    // 모든 예외 -> 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception e) {
        log.error("Unhandled exception", e);
        return wrap(HttpStatus.INTERNAL_SERVER_ERROR, " 내부 서버 오류가 발생했습니다. 관리자에게 문의 해주세요.");
    }
}
