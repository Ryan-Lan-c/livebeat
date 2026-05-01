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
    USER_NOT_FOUND("AUTH_006", "User not found", HttpStatus.NOT_FOUND),
    ACCESS_DENIED("AUTH_007", "Access denied", HttpStatus.FORBIDDEN),

    // Concert module
    CONCERT_NOT_FOUND("CONCERT_001", "Concert not found", HttpStatus.NOT_FOUND),
    SESSION_NOT_FOUND("CONCERT_002", "Concert session not found", HttpStatus.NOT_FOUND),
    ZONE_NOT_FOUND("CONCERT_003", "Ticket zone not found", HttpStatus.NOT_FOUND),
    CONCERT_DELETE_NOT_ALLOWED("CONCERT_004", "Only DRAFT concerts can be deleted", HttpStatus.UNPROCESSABLE_ENTITY),
    SESSION_DELETE_NOT_ALLOWED("CONCERT_005", "Only DRAFT sessions can be deleted", HttpStatus.UNPROCESSABLE_ENTITY),
    ZONE_DELETE_NOT_ALLOWED("CONCERT_006", "Cannot delete a zone that has sold or locked tickets", HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_STATUS_TRANSITION("CONCERT_007", "Invalid concert status transition", HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_SESSION_STATUS_TRANSITION("CONCERT_010", "Invalid session status transition", HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_FILE_TYPE("CONCERT_008", "Only image files are allowed", HttpStatus.BAD_REQUEST),
    STORAGE_UPLOAD_FAILED("CONCERT_009", "Failed to upload file to storage", HttpStatus.INTERNAL_SERVER_ERROR);

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
