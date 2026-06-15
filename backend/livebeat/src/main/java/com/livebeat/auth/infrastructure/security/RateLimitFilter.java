package com.livebeat.auth.infrastructure.security;

import com.livebeat.shared.ApiVersion;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * [auth] 認證端點 per-IP 限流 Filter（Redis 固定視窗計數）
 *
 * 負責：對 /auth/login、/register、/refresh 做 per-IP 限流，超過門檻回 429，緩解暴力破解 /
 *       撞庫 / refresh 濫用。以 Redis INCR+EXPIRE 計數，可在多實例間共享。
 * 設計：Redis 不可用時 fail-open（記 warn 後放行），避免因快取故障導致登入完全不可用。
 *      門檻外部化於 {@link RateLimitProperties}。per-account 鎖定屬後續強化，本版先做 per-IP。
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final StringRedisTemplate redis;
    private final RateLimitProperties props;
    private final List<Rule> rules;

    private record Rule(String path, int limit, Duration window) {}

    public RateLimitFilter(StringRedisTemplate redis, RateLimitProperties props) {
        this.redis = redis;
        this.props = props;
        this.rules = List.of(
                new Rule(ApiVersion.V1 + "/auth/login", props.loginPerMinute(), Duration.ofMinutes(1)),
                new Rule(ApiVersion.V1 + "/auth/register", props.registerPerHour(), Duration.ofHours(1)),
                new Rule(ApiVersion.V1 + "/auth/refresh", props.refreshPerHour(), Duration.ofHours(1))
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!props.enabled() || !"POST".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        Rule rule = ruleFor(request.getRequestURI());
        if (rule == null) {
            chain.doFilter(request, response);
            return;
        }
        String key = "rl:" + rule.path() + ":" + clientIp(request);
        if (allow(key, rule.limit(), rule.window())) {
            chain.doFilter(request, response);
        } else {
            writeTooManyRequests(response);
        }
    }

    private Rule ruleFor(String uri) {
        for (Rule r : rules) {
            if (r.path().equals(uri)) {
                return r;
            }
        }
        return null;
    }

    /** 固定視窗計數：第一次命中設定 TTL；超過門檻回 false。Redis 故障時 fail-open。 */
    private boolean allow(String key, int limit, Duration window) {
        try {
            Long count = redis.opsForValue().increment(key);
            if (count == null) {
                return true;
            }
            if (count == 1L) {
                redis.expire(key, window);
            }
            return count <= limit;
        } catch (RuntimeException e) {
            log.warn("Rate limiter Redis error on key {}; failing open", key, e);
            return true;
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For 可能是逗號分隔串，取第一個（最原始 client）
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        return request.getRemoteAddr();
    }

    private static void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"code\":\"RATE_LIMITED\",\"message\":\"Too many requests, please try again later\"}");
    }
}
