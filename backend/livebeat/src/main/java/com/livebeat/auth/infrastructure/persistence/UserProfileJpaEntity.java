package com.livebeat.auth.infrastructure.persistence;

import com.livebeat.auth.domain.model.UserProfile;
import com.livebeat.shared.persistence.TimestampedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * [auth] UserProfile JPA 實體
 *
 * 負責：對應 auth.user_profiles 資料表；PK 為 user_id（非自動產生，對應 users.id）；
 *       繼承 TimestampedEntity（created_at/updated_at）；提供 toDomain / fromDomain 轉換
 */
@Entity
@Table(name = "user_profiles", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileJpaEntity extends TimestampedEntity {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(length = 20)
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(columnDefinition = "text")
    private String address;

    UserProfile toDomain() {
        return UserProfile.builder()
                .userId(userId).avatarUrl(avatarUrl).bio(bio)
                .phone(phone).birthDate(birthDate).address(address)
                .createdAt(getCreatedAt()).updatedAt(getUpdatedAt())
                .build();
    }

    static UserProfileJpaEntity fromDomain(UserProfile profile) {
        return UserProfileJpaEntity.builder()
                .userId(profile.getUserId())
                .avatarUrl(profile.getAvatarUrl())
                .bio(profile.getBio())
                .phone(profile.getPhone())
                .birthDate(profile.getBirthDate())
                .address(profile.getAddress())
                .build();
    }
}
