package com.livebeat.auth.infrastructure.persistence;

import com.livebeat.auth.domain.model.RefreshToken;
import com.livebeat.shared.persistence.CreatedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenJpaEntity extends CreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    RefreshToken toDomain() {
        return RefreshToken.builder()
                .id(id).userId(userId).token(token)
                .expiresAt(expiresAt).revoked(revoked).createdAt(getCreatedAt())
                .build();
    }

    static RefreshTokenJpaEntity fromDomain(RefreshToken token) {
        return RefreshTokenJpaEntity.builder()
                .id(token.getId()).userId(token.getUserId()).token(token.getToken())
                .expiresAt(token.getExpiresAt()).revoked(token.isRevoked())
                .build();
    }
}
