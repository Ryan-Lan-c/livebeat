package com.livebeat.auth.application.service;

import com.livebeat.auth.application.dto.AuthResponse;
import com.livebeat.auth.application.dto.LoginRequest;
import com.livebeat.auth.application.dto.RegisterRequest;
import com.livebeat.auth.application.dto.UpdateProfileRequest;
import com.livebeat.auth.domain.model.RefreshToken;
import com.livebeat.auth.domain.model.User;
import com.livebeat.auth.domain.port.RefreshTokenRepository;
import com.livebeat.auth.domain.port.UserRepository;
import com.livebeat.shared.config.JwtProperties;
import com.livebeat.shared.exception.ApiException;
import com.livebeat.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * [auth] 認證業務邏輯服務
 *
 * 負責：使用者註冊、Email 登入驗證、Refresh Token 輪換、登出（撤銷 Token）、個人資料查詢與更新
 * 依賴：UserRepository, RefreshTokenRepository, JwtService, PasswordEncoder
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        User saved = userRepository.save(
                User.create(request.email(), request.username(),
                        passwordEncoder.encode(request.password()))
        );
        return issueTokens(saved);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!user.isEnabled()) {
            throw new ApiException(ErrorCode.ACCOUNT_DISABLED);
        }
        refreshTokenRepository.revokeAllByUserId(user.getId());
        return issueTokens(user);
    }

    public AuthResponse refresh(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_REFRESH_TOKEN));
        if (!token.isValid()) {
            throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        refreshTokenRepository.revokeAllByUserId(user.getId());
        return issueTokens(user);
    }

    public void logout(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue)
                .ifPresent(token -> refreshTokenRepository.revokeAllByUserId(token.getUserId()));
    }

    @Transactional(readOnly = true)
    public User getMe(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    public User updateMe(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        if (!user.getUsername().equals(request.username())
                && userRepository.existsByUsernameAndIdNot(request.username(), userId)) {
            throw new ApiException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        return userRepository.save(user.withUsername(request.username()));
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshTokenValue = UUID.randomUUID().toString();
        refreshTokenRepository.save(
                RefreshToken.create(user.getId(), refreshTokenValue,
                        jwtProperties.refreshTokenExpirationSeconds())
        );
        return new AuthResponse(accessToken, refreshTokenValue,
                user.getId(), user.getEmail(), user.getUsername(), user.getRole().name());
    }
}
