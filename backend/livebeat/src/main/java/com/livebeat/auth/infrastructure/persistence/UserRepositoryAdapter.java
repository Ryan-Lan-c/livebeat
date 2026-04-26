package com.livebeat.auth.infrastructure.persistence;

import com.livebeat.auth.domain.model.User;
import com.livebeat.auth.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * [auth] UserRepository Port 的 JPA 實作（Adapter Out）
 *
 * 負責：銜接 domain port 與 Spring Data JPA，轉換 JPA 實體與 domain model
 * 依賴：UserJpaRepository
 */
@Repository
@RequiredArgsConstructor
class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository jpa;

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(UserJpaEntity::toDomain);
    }

    @Override
    public User save(User user) {
        if (user.getId() != null) {
            return jpa.findById(user.getId())
                    .map(existing -> {
                        existing.setUsername(user.getUsername());
                        existing.setEmail(user.getEmail());
                        existing.setPasswordHash(user.getPasswordHash());
                        existing.setRole(user.getRole());
                        existing.setAuthProvider(user.getAuthProvider());
                        existing.setEnabled(user.isEnabled());
                        existing.setOrganizerId(user.getOrganizerId());
                        return jpa.save(existing).toDomain();
                    })
                    .orElseGet(() -> jpa.save(UserJpaEntity.fromDomain(user)).toDomain());
        }
        return jpa.save(UserJpaEntity.fromDomain(user)).toDomain();
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    public boolean existsByUsernameAndIdNot(String username, UUID excludeId) {
        return jpa.existsByUsernameAndIdNot(username, excludeId);
    }
}
