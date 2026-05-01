package com.livebeat.auth.domain.port;

import com.livebeat.auth.domain.model.UserProfile;

import java.util.Optional;
import java.util.UUID;

/**
 * [auth] UserProfile Repository Port（次要埠，由 Infrastructure 實作）
 *
 * 負責：定義使用者擴充個人資料的持久化操作契約
 */
public interface UserProfileRepository {
    Optional<UserProfile> findByUserId(UUID userId);
    UserProfile save(UserProfile profile);
}
