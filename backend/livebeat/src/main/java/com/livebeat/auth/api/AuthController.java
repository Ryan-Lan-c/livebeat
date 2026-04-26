package com.livebeat.auth.api;

import com.livebeat.auth.application.dto.AuthResponse;
import com.livebeat.auth.application.dto.LoginRequest;
import com.livebeat.auth.application.dto.RegisterRequest;
import com.livebeat.auth.application.dto.TokenResponse;
import com.livebeat.auth.application.service.AuthService;
import com.livebeat.shared.exception.ApiException;
import com.livebeat.shared.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60;

    private final AuthService authService;

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

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        clearRefreshTokenCookie(response);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    private String extractRefreshTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
