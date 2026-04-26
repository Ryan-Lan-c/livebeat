package com.livebeat.auth.domain.port;

import com.livebeat.auth.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    User save(User user);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
