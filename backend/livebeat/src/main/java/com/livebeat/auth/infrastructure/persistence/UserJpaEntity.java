package com.livebeat.auth.infrastructure.persistence;

import com.livebeat.auth.domain.model.AuthProvider;
import com.livebeat.auth.domain.model.User;
import com.livebeat.auth.domain.model.UserRole;
import com.livebeat.shared.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * [auth] User JPA 實體
 *
 * 負責：對應 auth.users 資料表；繼承 AuditedEntity（created_at/updated_at/created_by/updated_by）；
 *       提供 toDomain / fromDomain 轉換方法
 */
@Entity
@Table(name = "users", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJpaEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

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

    @Column(name = "organizer_id", columnDefinition = "uuid")
    private UUID organizerId;

    User toDomain() {
        return User.builder()
                .id(id).email(email).username(username).passwordHash(passwordHash)
                .role(role).authProvider(authProvider).enabled(enabled)
                .organizerId(organizerId)
                .createdBy(getCreatedBy()).updatedBy(getUpdatedBy())
                .createdAt(getCreatedAt()).updatedAt(getUpdatedAt())
                .build();
    }

    static UserJpaEntity fromDomain(User user) {
        return UserJpaEntity.builder()
                .id(user.getId()).email(user.getEmail()).username(user.getUsername())
                .passwordHash(user.getPasswordHash()).role(user.getRole())
                .authProvider(user.getAuthProvider()).enabled(user.isEnabled())
                .organizerId(user.getOrganizerId())
                .build();
    }
}
