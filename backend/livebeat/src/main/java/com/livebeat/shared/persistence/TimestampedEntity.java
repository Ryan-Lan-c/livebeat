package com.livebeat.shared.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * [shared] 含建立與更新時間的 JPA MappedSuperclass（第二層）
 *
 * 負責：提供 updated_at（TIMESTAMPTZ），由 Spring Data JPA Auditing 自動填入；繼承自 CreatedEntity
 */
@Getter
@MappedSuperclass
public abstract class TimestampedEntity extends CreatedEntity {

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
