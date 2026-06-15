package com.livebeat.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * [test] 整合測試共用基底：以 Testcontainers 啟動真實 PostgreSQL，
 * 使 Flyway 全鏈與 Hibernate ddl-auto=validate 對真實 Postgres 執行，
 * pg_trgm 等需真實 Postgres 的功能亦在此驗證。
 *
 * 採 singleton container 模式：在 static 區塊手動啟動單一容器、跨所有測試類別共用、
 * 不中途停止（由 Testcontainers Ryuk 於 JVM 結束時清理）。
 * 這避免「static @Container 於共用基底跨多個測試類別時被提前 teardown、
 * 而 Spring context 快取仍指向已停容器」導致的連線失敗。
 *
 * 注意：整合測試需要可用的 Docker；無 Docker 環境執行時會在容器啟動處失敗。
 */
@SpringBootTest
public abstract class PostgresIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
