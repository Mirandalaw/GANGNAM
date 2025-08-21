package com.gangnam.coupon.web.response;

/**
 * 공통 API 응답 포맷
 *
 * 모든 Controller 응답을 일관된 형태로 감싸기 위해
 * @param success 요청 성공 여부
 * @param data 성공 시 반환할 데이터 ( 제네릭 타입 )
 * @param error 실패 시 에러 메시지
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        String error) {
    /**
     * 성공 응답
     * @param data 성공 시 반환할 데이터
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 실패 응답
     * @param error 실패 원인
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> error(String error) {
        return new ApiResponse<>(false, null, error);
    }
}
