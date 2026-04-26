package com.livebeat.auth.infrastructure.persistence;

import com.livebeat.auth.domain.model.AuthProvider;
import com.livebeat.auth.domain.model.User;
import com.livebeat.auth.domain.model.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    User toDomain() {
        return User.builder()
                .id(id).email(email).username(username).passwordHash(passwordHash)
                .role(role).authProvider(authProvider).enabled(enabled)
                .createdAt(createdAt).updatedAt(updatedAt)
                .build();
    }

    static UserJpaEntity fromDomain(User user) {
        return UserJpaEntity.builder()
                .id(user.getId()).email(user.getEmail()).username(user.getUsername())
                .passwordHash(user.getPasswordHash()).role(user.getRole())
                .authProvider(user.getAuthProvider()).enabled(user.isEnabled())
                .createdAt(user.getCreatedAt()).updatedAt(user.getUpdatedAt())
                .build();
    }
}
