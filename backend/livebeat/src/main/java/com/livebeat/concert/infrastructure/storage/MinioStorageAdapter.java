package com.livebeat.concert.infrastructure.storage;

import com.livebeat.concert.domain.port.StoragePort;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.net.URI;

/**
 * [concert] StoragePort 的 MinIO 實作（使用 AWS SDK v2）
 *
 * 負責：透過 S3-compatible API 上傳與刪除檔案；path-style access 啟用以相容 MinIO
 */
@Component
@EnableConfigurationProperties(MinioProperties.class)
public class MinioStorageAdapter implements StoragePort {

    private final S3Client s3;
    private final MinioProperties properties;

    public MinioStorageAdapter(MinioProperties properties) {
        this.properties = properties;
        this.s3 = S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    @Override
    public String store(String key, InputStream data, long contentLength, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();
        s3.putObject(request, RequestBody.fromInputStream(data, contentLength));
        return properties.endpoint() + "/" + properties.bucket() + "/" + key;
    }

    @Override
    public void remove(String key) {
        s3.deleteObject(b -> b.bucket(properties.bucket()).key(key));
    }
}
