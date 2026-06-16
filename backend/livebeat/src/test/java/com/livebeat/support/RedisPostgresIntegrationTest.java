package com.livebeat.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * [test] 整合測試基底：在 PostgresIntegrationTest 之上再啟動真實 Redis，
 * 供 order 庫存（Redis Lua 原子扣減）的併發與復原測試使用。
 *
 * 同樣採 singleton container：static 區塊啟動、跨測試類別共用，由 Ryuk 於 JVM 結束清理。
 */
public abstract class RedisPostgresIntegrationTest extends PostgresIntegrationTest {

    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    static {
        REDIS.start();
    }

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        // 測試用 Redis 容器未設密碼，覆寫掉 application.yml 的預設密碼
        registry.add("spring.data.redis.password", () -> "");
    }
}
