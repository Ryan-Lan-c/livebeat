package com.livebeat.auth.domain.port;

import com.livebeat.auth.domain.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);
    RefreshToken save(RefreshToken token);
    void revokeAllByUserId(Long userId);
    void deleteExpired();
}
