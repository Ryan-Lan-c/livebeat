package com.livebeat.auth.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * [auth] 認證端點限流設定（app.rate-limit.*）
 *
 * 負責：外部化各認證端點的 per-IP 限流門檻，可由環境變數覆寫；預設值見下方 @DefaultValue。
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("10") int loginPerMinute,
        @DefaultValue("5") int registerPerHour,
        @DefaultValue("60") int refreshPerHour
) {}
