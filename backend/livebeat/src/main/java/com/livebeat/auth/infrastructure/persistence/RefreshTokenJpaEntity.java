package com.livebeat.auth.infrastructure.persistence;

import com.livebeat.auth.domain.model.RefreshToken;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    RefreshToken toDomain() {
        return RefreshToken.builder()
                .id(id).userId(userId).token(token)
                .expiresAt(expiresAt).revoked(revoked).createdAt(createdAt)
                .build();
    }

    static RefreshTokenJpaEntity fromDomain(RefreshToken token) {
        return RefreshTokenJpaEntity.builder()
                .id(token.getId()).userId(token.getUserId()).token(token.getToken())
                .expiresAt(token.getExpiresAt()).revoked(token.isRevoked())
                .createdAt(token.getCreatedAt())
                .build();
    }
}
