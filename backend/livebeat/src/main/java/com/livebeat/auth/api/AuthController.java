package com.livebeat.auth.api;

import com.livebeat.auth.application.dto.AuthResponse;
import com.livebeat.auth.api.dto.LoginRequest;
import com.livebeat.auth.application.dto.MeResponse;
import com.livebeat.auth.api.dto.RegisterRequest;
import com.livebeat.auth.application.dto.TokenResponse;
import com.livebeat.auth.api.dto.UpdateMeRequest;
import com.livebeat.auth.application.dto.UserProfileResponse;
import com.livebeat.auth.application.service.AuthService;
import com.livebeat.auth.application.service.ProfileService;
import com.livebeat.shared.ApiVersion;
import com.livebeat.shared.config.JwtProperties;
import com.livebeat.shared.exception.ApiException;
import com.livebeat.shared.exception.ErrorCode;
import com.livebeat.shared.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * [auth] 認證 REST API 控制器
 *
 * 負責：處理使用者註冊、登入、Token 刷新、登出；Refresh Token 透過 HttpOnly Cookie 傳遞；個人資料查詢與更新
 * 對應路由：POST /api/v1/auth/register, /login, /refresh, /logout；GET /PUT /api/v1/auth/me
 * 依賴：AuthService, ProfileService, JwtProperties
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String COOKIE_PATH = "/api/v1/auth";

    private final AuthService authService;
    private final ProfileService profileService;
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest request,
                                  HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        setRefreshTokenCookie(response, authResponse.refreshToken());
        return TokenResponse.from(authResponse);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request,
                               HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        setRefreshTokenCookie(response, authResponse.refreshToken());
        return TokenResponse.from(authResponse);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(HttpServletRequest request,
                                 HttpServletResponse response) {
        String token = extractRefreshTokenCookie(request);
        AuthResponse authResponse = authService.refresh(token);
        setRefreshTokenCookie(response, authResponse.refreshToken());
        return TokenResponse.from(authResponse);
    }

    @GetMapping("/me")
    public MeResponse getMe(@AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse profile = profileService.getUserProfile(principal.userId());
        return MeResponse.from(authService.getMe(principal.userId()), profile);
    }

    @PutMapping("/me")
    public MeResponse updateMe(@AuthenticationPrincipal UserPrincipal principal,
                               @Valid @RequestBody UpdateMeRequest request) {
        UserProfileResponse profile = profileService.getUserProfile(principal.userId());
        return MeResponse.from(authService.updateMe(principal.userId(), request), profile);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                    .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                    .map(c -> c.getValue())
                    .findFirst()
                    .ifPresent(authService::logout);
        }
        clearRefreshTokenCookie(response);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie", buildCookieHeader(token, jwtProperties.refreshTokenExpirationSeconds()));
    }

    private String extractRefreshTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(c -> c.getValue())
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildCookieHeader("", 0));
    }

    private String buildCookieHeader(String value, long maxAge) {
        StringBuilder sb = new StringBuilder();
        sb.append(REFRESH_TOKEN_COOKIE).append("=").append(value);
        sb.append("; HttpOnly");
        sb.append("; Path=").append(COOKIE_PATH);
        sb.append("; Max-Age=").append(maxAge);
        sb.append("; SameSite=Lax");
        if (jwtProperties.cookieSecure()) {
            sb.append("; Secure");
        }
        return sb.toString();
    }
}
