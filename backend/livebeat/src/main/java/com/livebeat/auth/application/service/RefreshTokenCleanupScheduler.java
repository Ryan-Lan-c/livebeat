package com.livebeat.auth.application.service;

import com.livebeat.auth.domain.port.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * [auth] 過期 Refresh Token 清理排程
 *
 * 負責：定期刪除已過期的 refresh token，避免資料表無限累積。
 * 備註：採單實例 @Scheduled；多實例部署時應改用分散式排程（如 Quartz 叢集或 ShedLock）避免重複執行。
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupScheduler.class);

    private final RefreshTokenRepository refreshTokenRepository;

    /** 每小時整點清除過期 refresh token。 */
    @Scheduled(cron = "0 0 * * * *")
    public void purgeExpiredTokens() {
        refreshTokenRepository.deleteExpired();
        log.debug("Purged expired refresh tokens");
    }
}
