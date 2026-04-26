package com.livebeat.auth.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

/**
 * [auth] 使用者領域模型（純 Java，無框架依賴）
 *
 * 負責：封裝使用者核心屬性與建立邏輯；透過 User.create() 建立新的 LOCAL 帳號
 */
@Getter
@Builder
@With
public class User {
    private final UUID id;
    private final String email;
    private final String username;
    private final String passwordHash;
    private final UserRole role;
    private final AuthProvider authProvider;
    private final boolean enabled;
    private final UUID organizerId;
    private final UUID createdBy;
    private final UUID updatedBy;
    private final Instant createdAt;
    private final Instant updatedAt;

    public static User create(String email, String username, String passwordHash) {
        return User.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordHash)
                .role(UserRole.USER)
                .authProvider(AuthProvider.LOCAL)
                .enabled(true)
                .build();
    }
}
