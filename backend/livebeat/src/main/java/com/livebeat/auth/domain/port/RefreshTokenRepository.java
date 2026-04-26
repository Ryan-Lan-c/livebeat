package com.livebeat.auth.domain.port;

import com.livebeat.auth.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

/**
 * [auth] Refresh Token 資料存取介面（Port）
 *
 * 負責：定義 Refresh Token 的存取契約，由 infrastructure 層實作
 */
public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);
    RefreshToken save(RefreshToken token);
    void revokeAllByUserId(UUID userId);
    void deleteExpired();
}
