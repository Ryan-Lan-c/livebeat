package com.livebeat.auth.domain.port;

import com.livebeat.auth.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * [auth] 使用者資料存取介面（Port）
 *
 * 負責：定義使用者資料的存取契約，由 infrastructure 層實作
 */
public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    User save(User user);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, UUID excludeId);
}
