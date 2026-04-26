package com.livebeat.shared.config;

import com.livebeat.shared.persistence.AuditedEntity;
import com.livebeat.shared.security.UserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * [shared] JPA Auditing 設定
 *
 * 負責：啟用 @EnableJpaAuditing；提供 AuditorAware&lt;UUID&gt; Bean，
 *       從 SecurityContext 取得當前登入者 UUID，無認證時回傳全零 UUID（系統操作）
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()
                    || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
                return Optional.of(AuditedEntity.SYSTEM_USER_ID);
            }
            return Optional.of(principal.userId());
        };
    }
}
