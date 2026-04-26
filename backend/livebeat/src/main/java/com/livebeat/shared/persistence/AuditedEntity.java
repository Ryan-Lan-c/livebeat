package com.livebeat.shared.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.util.UUID;

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
