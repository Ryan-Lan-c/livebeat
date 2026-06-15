package com.livebeat.shared.exception;

import com.livebeat.shared.exception.GlobalExceptionHandler.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [shared] GlobalExceptionHandler 單元測試（對應 P2-14）
 *
 * 負責：驗證各例外映射到正確的 HTTP 狀態與錯誤碼；未預期例外不洩漏內部細節。
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void apiException_maps_to_declared_status_and_code() {
        ResponseEntity<ErrorResponse> resp = handler.handleApiException(new ApiException(ErrorCode.CONCERT_NOT_FOUND));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().code()).isEqualTo(ErrorCode.CONCERT_NOT_FOUND.getCode());
    }

    @Test
    void data_integrity_violation_maps_to_409() {
        ResponseEntity<ErrorResponse> resp = handler.handleDataIntegrity(
                new DataIntegrityViolationException("duplicate key"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody().code()).isEqualTo("CONFLICT");
        assertThat(resp.getBody().message()).doesNotContain("duplicate key");
    }

    @Test
    void optimistic_lock_failure_maps_to_409() {
        ResponseEntity<ErrorResponse> resp = handler.handleOptimisticLock(
                new OptimisticLockingFailureException("version mismatch"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody().code()).isEqualTo("CONCURRENT_MODIFICATION");
    }

    @Test
    void max_upload_size_maps_to_413() {
        ResponseEntity<ErrorResponse> resp = handler.handleMaxUploadSize(
                new MaxUploadSizeExceededException(5_242_880L));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(resp.getBody().code()).isEqualTo("FILE_TOO_LARGE");
    }

    @Test
    void unexpected_exception_maps_to_500_without_leaking_details() {
        ResponseEntity<ErrorResponse> resp = handler.handleUnexpected(new RuntimeException("NPE at secret line"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody().message()).doesNotContain("secret");
    }
}
