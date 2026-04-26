package com.livebeat.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * [shared] JWT 與 Cookie 設定屬性
 *
 * 負責：從 application.yml 的 app.jwt.* 讀取 JWT secret、Token TTL、Cookie Secure flag
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationSeconds,
        long refreshTokenExpirationSeconds,
        boolean cookieSecure
) {}
