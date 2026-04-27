package com.livebeat.concert.infrastructure.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * [concert] MinioStorageAdapter 整合測試
 *
 * 負責：驗證上傳與刪除操作可正確與 MinIO（Testcontainers）互動
 * 若 Docker 環境不可用則自動略過（不視為失敗）
 */
@Testcontainers(disabledWithoutDocker = true)
class MinioStorageAdapterTest {

    private static final String ACCESS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin";
    private static final String BUCKET = "livebeat-test";

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> MINIO = new GenericContainer<>(
            DockerImageName.parse("minio/minio:latest"))
            .withCommand("server", "/data")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY);

    private MinioStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
                "Docker not available; skipping MinIO integration tests");

        String endpoint = "http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(9000);

        S3Client setupClient = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
        try {
            setupClient.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
        } catch (BucketAlreadyOwnedByYouException ignored) {
            // bucket already exists from the previous test in this run
        }

        MinioProperties props = new MinioProperties(endpoint, ACCESS_KEY, SECRET_KEY, BUCKET);
        adapter = new MinioStorageAdapter(props);
    }

    @Test
    void store_returns_accessible_url() {
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
        String key = "test/image.txt";

        String url = adapter.store(key, new ByteArrayInputStream(content), content.length, "text/plain");

        assertThat(url).contains(BUCKET).contains(key);
    }

    @Test
    void remove_does_not_throw() {
        byte[] content = "to delete".getBytes(StandardCharsets.UTF_8);
        String key = "test/delete-me.txt";
        adapter.store(key, new ByteArrayInputStream(content), content.length, "text/plain");

        adapter.remove(key);
    }
}
