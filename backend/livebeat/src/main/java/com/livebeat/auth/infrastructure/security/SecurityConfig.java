package com.livebeat.auth.infrastructure.security;

import com.livebeat.shared.ApiVersion;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * [auth] Spring Security 設定
 *
 * 負責：Stateless JWT 安全策略、公開路由白名單（register/login/refresh/logout）、密碼編碼器；
 *       注入 JwtAuthFilter 與認證端點限流 RateLimitFilter；設定安全標頭（HSTS/CSP/frameOptions/nosniff）與 CORS。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties rateLimitProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource) throws Exception {
        RateLimitFilter rateLimitFilter = new RateLimitFilter(redisTemplate, rateLimitProperties);
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .headers(headers -> headers
                        // API 不需載入任何資源、也不應被嵌入 iframe
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31_536_000))
                        // X-Content-Type-Options: nosniff 為 Spring Security 預設啟用
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        // 未認證 → 401；已認證但權限不足 → 403（語意正確，避免前端把 403 當 401 去 refresh）
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                )
                .authorizeHttpRequests(auth -> auth
                        // 放行 ERROR/ASYNC dispatch：sendError 會轉發到 /error，若不放行，error dispatch
                        // 會被當未認證而把原本的 403/狀態覆蓋成 401（自訂 SecurityFilterChain 的經典陷阱）。
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC).permitAll()
                        .requestMatchers(
                                ApiVersion.V1 + "/auth/register",
                                ApiVersion.V1 + "/auth/login",
                                ApiVersion.V1 + "/auth/refresh",
                                ApiVersion.V1 + "/auth/logout",
                                "/actuator/health"
                        ).permitAll()
                        .requestMatchers(
                                ApiVersion.V1 + "/concerts",
                                ApiVersion.V1 + "/concerts/**"
                        ).permitAll()
                        .requestMatchers(ApiVersion.V1 + "/admin/**")
                                .hasAnyRole("ADMIN", "ORGANIZER")
                        .anyRequest().authenticated()
                )
                // 限流須在認證之前先行；rate limit 與 JWT filter 皆置於 UsernamePasswordAuthenticationFilter 之前
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * CORS 設定：僅允許白名單來源（由 app.cors.allowed-origins 設定，預設開發來源）。
     * allowCredentials=true 以支援前端帶 HttpOnly refresh cookie（SameSite=Lax）跨來源呼叫 /auth/refresh。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
