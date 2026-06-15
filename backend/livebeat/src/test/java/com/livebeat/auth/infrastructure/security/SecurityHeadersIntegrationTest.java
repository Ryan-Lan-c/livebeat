package com.livebeat.auth.infrastructure.security;

import com.livebeat.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [auth] 安全標頭與 CORS 整合測試（對應 P1-06）
 *
 * 以真實 HTTP（RANDOM_PORT + JDK HttpClient）驗證回應標頭與 CORS 白名單行為。
 * 註：HSTS 由 Spring Security 僅對 HTTPS 請求輸出，純 HTTP 測試環境不驗證，已於 SecurityConfig 設定。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityHeadersIntegrationTest extends PostgresIntegrationTest {

    @Value("${local.server.port}")
    int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> get(String path, String origin) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET();
        if (origin != null) {
            b.header("Origin", origin);
        }
        return client.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void response_includes_security_headers() throws Exception {
        HttpResponse<String> res = get("/api/v1/concerts", null);

        assertThat(res.headers().firstValue("Content-Security-Policy")).isPresent();
        assertThat(res.headers().firstValue("X-Frame-Options")).hasValue("DENY");
        assertThat(res.headers().firstValue("X-Content-Type-Options")).hasValue("nosniff");
    }

    @Test
    void cors_allows_whitelisted_origin() throws Exception {
        HttpResponse<String> res = get("/api/v1/concerts", "http://localhost:5173");

        assertThat(res.headers().firstValue("Access-Control-Allow-Origin"))
                .hasValue("http://localhost:5173");
    }

    @Test
    void cors_rejects_unknown_origin() throws Exception {
        HttpResponse<String> res = get("/api/v1/concerts", "http://evil.example");

        // 非白名單來源：Spring CORS 不會回傳 Access-Control-Allow-Origin
        assertThat(res.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
    }
}
