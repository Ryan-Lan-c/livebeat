package com.livebeat.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * [auth] RateLimitFilter 單元測試（對應 P1-05）
 *
 * 驗證：認證端點超過 per-IP 門檻回 429 並中止鏈、門檻內放行、非目標路徑/GET 不限流、
 *       Redis 故障時 fail-open（放行而非擋住登入）。
 */
class RateLimitFilterTest {

    private final RateLimitProperties props = new RateLimitProperties(true, 10, 5, 60);

    @SuppressWarnings("unchecked")
    private RateLimitFilter filterReturning(Long incrementResult) {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.increment(anyString())).thenReturn(incrementResult);
        return new RateLimitFilter(redis, props);
    }

    private MockHttpServletRequest post(String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", uri);
        req.setRemoteAddr("203.0.113.5");
        return req;
    }

    @Test
    void allows_login_request_under_limit() throws Exception {
        RateLimitFilter filter = filterReturning(1L);
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(post("/api/v1/auth/login"), res, chain);

        verify(chain).doFilter(any(), any());
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    void blocks_login_request_over_limit_with_429() throws Exception {
        RateLimitFilter filter = filterReturning(11L); // > loginPerMinute(10)
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(post("/api/v1/auth/login"), res, chain);

        verify(chain, never()).doFilter(any(), any());
        assertThat(res.getStatus()).isEqualTo(429);
        assertThat(res.getContentAsString()).contains("RATE_LIMITED");
    }

    @Test
    void does_not_rate_limit_non_auth_paths() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        RateLimitFilter filter = new RateLimitFilter(redis, props);
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(post("/api/v1/concerts"), res, chain);

        verify(chain).doFilter(any(), any());
        verifyNoInteractions(redis);
    }

    @Test
    void does_not_rate_limit_get_requests() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        RateLimitFilter filter = new RateLimitFilter(redis, props);
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest get = new MockHttpServletRequest("GET", "/api/v1/auth/login");

        filter.doFilter(get, res, chain);

        verify(chain).doFilter(any(), any());
        verifyNoInteractions(redis);
    }

    @Test
    @SuppressWarnings("unchecked")
    void fails_open_when_redis_errors() throws Exception {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.increment(anyString())).thenThrow(new RuntimeException("redis down"));
        RateLimitFilter filter = new RateLimitFilter(redis, props);
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(post("/api/v1/auth/login"), res, chain);

        verify(chain).doFilter(any(), any());
        assertThat(res.getStatus()).isEqualTo(200);
    }
}
