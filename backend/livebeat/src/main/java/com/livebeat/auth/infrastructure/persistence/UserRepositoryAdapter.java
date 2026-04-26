package com.livebeat.auth.infrastructure.persistence;

import com.livebeat.auth.domain.model.User;
import com.livebeat.auth.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository jpa;

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findById(id).map(UserJpaEntity::toDomain);
    }

    @Override
    public User save(User user) {
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
}
