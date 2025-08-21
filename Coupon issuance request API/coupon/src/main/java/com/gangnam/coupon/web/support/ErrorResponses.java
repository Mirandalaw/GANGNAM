package com.gangnam.coupon.web.support;

import com.gangnam.coupon.web.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * API 오류 응답/메시지 포맷 유틸리티.
 * - 예외 핸들러에서 중복되는 래핑/메시지 조립을 분리하여 재사용.
 * - 향후 i18n/에러코드 표준화로 확장 가능.
 */
public final class ErrorResponses {

    private ErrorResponses() {}

    /** 상태코드 + 메시지를 ApiResponse.error 로 감싸서 반환 */
    public static ResponseEntity<ApiResponse<Void>> wrap(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }

    /** Bean Validation 위반들을 사람이 읽기 쉬운 문자열로 병합 */
    public static String formatViolations(Set<? extends ConstraintViolation<?>> violations) {
        if (violations == null || violations.isEmpty()) return "";
        return violations.stream()
                .map(ErrorResponses::formatViolation)
                .collect(Collectors.joining(", "));
    }

    private static String formatViolation(ConstraintViolation<?> v) {
        String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
        String msg = v.getMessage() != null ? v.getMessage() : "유효하지 않은 값";
        return path.isBlank() ? msg : (path + ": " + msg);
    }
}
