package com.livebeat.auth.infrastructure.security;

import com.livebeat.auth.application.service.JwtService;
import com.livebeat.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [auth] Web 層授權邊界整合測試
 *
 * 以真實 HTTP（RANDOM_PORT + JDK HttpClient）驗證 URL matcher 的授權不變量：
 *   - 公開端點免認證可達；
 *   - /admin/** 未認證被拒；非 ADMIN/ORGANIZER 角色被拒；ADMIN/ORGANIZER 可達 200。
 * 角色 authorities 來自 DB（JwtAuthFilter 以 token 的 email 載入使用者），故 seed 使用者、
 * 並用應用程式同一把金鑰的 JwtService 簽發 token。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityAuthorizationIntegrationTest extends PostgresIntegrationTest {

    @Value("${local.server.port}")
    int port;

    @Autowired
    JwtService jwtService;

    @Autowired
    JdbcTemplate jdbc;

    private final HttpClient client = HttpClient.newHttpClient();

    /** 在 DB 建立指定角色的使用者，回傳其有效的 access token。 */
    private String tokenForRole(String role) {
        UUID userId = UUID.randomUUID();
        String email = "authz-" + userId + "@test.com";
        jdbc.update("INSERT INTO auth.users (id, email, username, role) VALUES (?, ?, ?, ?)",
                userId, email, "u-" + userId, role);
        return jwtService.generateAccessToken(userId, email, role);
    }

    private int statusOf(String path, String bearerToken) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET();
        if (bearerToken != null) {
            b.header("Authorization", "Bearer " + bearerToken);
        }
        return client.send(b.build(), HttpResponse.BodyHandlers.ofString()).statusCode();
    }

    @Test
    void public_concerts_endpoint_is_accessible_without_auth() throws Exception {
        assertThat(statusOf("/api/v1/concerts", null)).isEqualTo(200);
    }

    @Test
    void admin_endpoint_requires_authentication() throws Exception {
        assertThat(statusOf("/api/v1/admin/concerts", null)).isEqualTo(401);
    }

    @Test
    void admin_endpoint_is_forbidden_for_user_role() throws Exception {
        // 已認證但角色不足 → 403（非 401；見 SecurityConfig 放行 ERROR dispatch 的修正）
        assertThat(statusOf("/api/v1/admin/concerts", tokenForRole("USER"))).isEqualTo(403);
    }

    @Test
    void admin_endpoint_is_allowed_for_admin_role() throws Exception {
        assertThat(statusOf("/api/v1/admin/concerts", tokenForRole("ADMIN"))).isEqualTo(200);
    }

    @Test
    void admin_endpoint_is_allowed_for_organizer_role() throws Exception {
        assertThat(statusOf("/api/v1/admin/concerts", tokenForRole("ORGANIZER"))).isEqualTo(200);
    }
}
