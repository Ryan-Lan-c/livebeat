package com.livebeat.auth.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * [auth] 使用者擴充個人資料領域模型（純 Java，無框架依賴）
 *
 * 負責：封裝使用者的 avatar、bio、phone、birthDate、address；對應 auth.user_profiles 資料表（1:1 關聯 users）
 */
@Getter
@Builder
@With
public class UserProfile {
    private final UUID userId;
    private final String avatarUrl;
    private final String bio;
    private final String phone;
    private final LocalDate birthDate;
    private final String address;
    private final Instant createdAt;
    private final Instant updatedAt;
}
