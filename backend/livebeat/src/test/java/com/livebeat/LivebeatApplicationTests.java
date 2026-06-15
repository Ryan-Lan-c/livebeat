package com.livebeat;

import com.livebeat.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * 應用程式 context 載入測試：對真實 PostgreSQL（Testcontainers）啟動，
 * 驗證 Flyway 全鏈套用與 Hibernate ddl-auto=validate 通過（含 ticket_zones.version 等新欄位）。
 */
class LivebeatApplicationTests extends PostgresIntegrationTest {
    @Test
    void contextLoads() {}
}
