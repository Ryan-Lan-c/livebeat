package com.livebeat.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * [shared] API 錯誤碼列舉
 *
 * 負責：定義所有業務邏輯錯誤的錯誤碼、訊息與 HTTP 狀態碼
 */
public enum ErrorCode {
    EMAIL_ALREADY_EXISTS("AUTH_001", "Email already exists", HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS("AUTH_002", "Username already exists", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("AUTH_003", "Invalid email or password", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED("AUTH_004", "Account is disabled", HttpStatus.FORBIDDEN),
    INVALID_REFRESH_TOKEN("AUTH_005", "Invalid or expired refresh token", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("AUTH_006", "User not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getStatus() { return status; }
}
