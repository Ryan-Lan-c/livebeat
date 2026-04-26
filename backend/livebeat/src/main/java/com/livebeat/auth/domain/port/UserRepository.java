package com.livebeat.auth.domain.port;

import com.livebeat.auth.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    User save(User user);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
