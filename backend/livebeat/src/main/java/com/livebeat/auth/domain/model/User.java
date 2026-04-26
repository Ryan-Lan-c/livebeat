package com.livebeat.auth.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDateTime;

@Getter
@Builder
@With
public class User {
    private final Long id;
    private final String email;
    private final String username;
    private final String passwordHash;
    private final UserRole role;
    private final AuthProvider authProvider;
    private final boolean enabled;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static User create(String email, String username, String passwordHash) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordHash)
                .role(UserRole.USER)
                .authProvider(AuthProvider.LOCAL)
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
