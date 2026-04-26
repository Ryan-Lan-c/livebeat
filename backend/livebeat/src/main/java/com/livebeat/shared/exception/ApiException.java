package com.livebeat.shared.exception;

/**
 * [shared] 業務邏輯例外
 *
 * 負責：攜帶 ErrorCode，由 GlobalExceptionHandler 統一轉為 API 錯誤回應
 */
public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
