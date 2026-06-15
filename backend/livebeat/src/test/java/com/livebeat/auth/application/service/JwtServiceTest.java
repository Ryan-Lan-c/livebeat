package com.livebeat.auth.application.service;

import com.livebeat.shared.config.JwtProperties;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * [auth] JwtService 單元測試
 *
 * 負責：驗證啟動時的 secret 強度檢查（fail-fast）、token 簽發/驗證 round-trip、偽造金鑰 token 被拒（對應 P0-02）
 */
class JwtServiceTest {

    private static final String VALID_SECRET = "a-test-secret-key-that-is-definitely-long-enough-256bit";

    private JwtService service() {
        return new JwtService(new JwtProperties(VALID_SECRET, 900, 604800, false));
    }

    @Test
    void rejects_null_secret_at_construction() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties(null, 900, 604800, false)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejects_blank_secret_at_construction() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties("   ", 900, 604800, false)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejects_secret_shorter_than_32_bytes() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties("too-short-secret", 900, 604800, false)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void generates_token_and_extracts_claims() {
        JwtService svc = service();
        UUID userId = UUID.randomUUID();

        String token = svc.generateAccessToken(userId, "user@test.com", "ADMIN");

        assertThat(svc.isTokenValid(token)).isTrue();
        assertThat(svc.extractEmail(token)).isEqualTo("user@test.com");
        assertThat(svc.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void malformed_token_is_invalid() {
        assertThat(service().isTokenValid("not-a-real-jwt")).isFalse();
    }

    @Test
    void token_signed_with_a_different_key_is_rejected() {
        JwtService signer = service();
        JwtService verifier = new JwtService(
                new JwtProperties("a-totally-different-secret-key-also-32-bytes-or-more", 900, 604800, false));

        String token = signer.generateAccessToken(UUID.randomUUID(), "user@test.com", "USER");

        assertThat(verifier.isTokenValid(token)).isFalse();
    }
}
