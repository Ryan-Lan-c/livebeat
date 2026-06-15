package com.livebeat.auth.application.service;

import com.livebeat.shared.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * [auth] JWT 產生與驗證服務
 *
 * 負責：產生 Access Token、驗證 Token 合法性、解析 Token 中的使用者資訊
 * 依賴：JwtProperties（secret、TTL 設定）
 */
@Service
public class JwtService {
    /** HS256 要求的最小金鑰長度（256-bit）。 */
    private static final int MIN_SECRET_BYTES = 32;

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    /**
     * 啟動時驗證 JWT secret 並建立簽章金鑰。
     * secret 為空或長度不足時直接拋例外，使應用程式 fail-fast 不啟動，
     * 避免以不安全的密鑰簽發可被偽造的 token。
     */
    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = buildSigningKey(jwtProperties.secret());
    }

    private static SecretKey buildSigningKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "app.jwt.secret (JWT_SECRET) 未設定：請以環境變數 / Secret Manager 注入高熵密鑰，"
                            + "應用程式拒絕以不安全的預設值啟動。");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret (JWT_SECRET) 長度不足：HS256 需至少 " + MIN_SECRET_BYTES
                            + " bytes（256-bit），目前僅 " + keyBytes.length + " bytes。");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UUID userId, String email, String role) {
        return buildToken(
                Map.of("uid", userId.toString(), "role", role),
                email,
                jwtProperties.accessTokenExpirationSeconds() * 1000L
        );
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaim(token, c -> c.get("uid", String.class)));
    }

    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(
                Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
        );
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }
}
