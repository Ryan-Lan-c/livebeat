package com.livebeat.concert.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * [concert] MinIO 連線設定（對應 application.yml app.storage.minio）
 */
@ConfigurationProperties("app.storage.minio")
public record MinioProperties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucket
) {}
