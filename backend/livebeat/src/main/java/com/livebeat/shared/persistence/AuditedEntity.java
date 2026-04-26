package com.livebeat.shared.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.util.UUID;

/**
 * [shared] 含審計欄位的 JPA MappedSuperclass（第三層）
 *
 * 負責：提供 created_by、updated_by（UUID）；系統操作填全零 UUID（SYSTEM_USER_ID）
 *       繼承自 TimestampedEntity（created_at、updated_at）
 */
@Getter
@MappedSuperclass
public abstract class AuditedEntity extends TimestampedEntity {

    public static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, columnDefinition = "uuid")
    private UUID updatedBy;
}
