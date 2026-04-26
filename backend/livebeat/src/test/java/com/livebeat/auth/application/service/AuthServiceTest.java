package com.livebeat.auth.application.service;

import com.livebeat.auth.application.dto.LoginRequest;
import com.livebeat.auth.application.dto.RegisterRequest;
import com.livebeat.auth.application.dto.UpdateProfileRequest;
import com.livebeat.auth.domain.model.AuthProvider;
import com.livebeat.auth.domain.model.RefreshToken;
import com.livebeat.auth.domain.model.User;
import com.livebeat.auth.domain.model.UserRole;
import com.livebeat.auth.domain.port.RefreshTokenRepository;
import com.livebeat.auth.domain.port.UserRepository;
import com.livebeat.shared.config.JwtProperties;
import com.livebeat.shared.exception.ApiException;
import com.livebeat.shared.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * [auth] AuthService 單元測試
 *
 * 負責：驗證註冊、登入、refresh、登出、個人資料更新的業務邏輯與錯誤處理
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock JwtService jwtService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtProperties jwtProperties;

    @InjectMocks AuthService authService;

    // ── register ─────────────────────────────────────────────────────────

    @Test
    void register_succeeds_for_new_user() {
        RegisterRequest req = new RegisterRequest("new@test.com", "newuser", "Password1!");
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(buildUser("new@test.com", "newuser"));
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access_token");
        when(jwtProperties.refreshTokenExpirationSeconds()).thenReturn(604800L);
        when(refreshTokenRepository.save(any())).thenReturn(buildRefreshToken());

        var result = authService.register(req);

        assertThat(result.email()).isEqualTo("new@test.com");
        assertThat(result.accessToken()).isEqualTo("access_token");
    }

    @Test
    void register_fails_when_email_exists() {
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("dup@test.com", "user", "Password1!")))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    void register_fails_when_username_exists() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("x@test.com", "taken", "Password1!")))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USERNAME_ALREADY_EXISTS);
    }

    // ── login ─────────────────────────────────────────────────────────────

    @Test
    void login_fails_with_wrong_password() {
        User user = buildUser("u@test.com", "user");
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("u@test.com", "wrong")))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void login_fails_when_account_disabled() {
        User disabled = User.builder().id(UUID.randomUUID()).email("d@test.com")
                .username("duser").passwordHash("hashed").role(UserRole.USER)
                .authProvider(AuthProvider.LOCAL).enabled(false).build();
        when(userRepository.findByEmail("d@test.com")).thenReturn(Optional.of(disabled));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("d@test.com", "Password1!")))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ACCOUNT_DISABLED);
    }

    // ── refresh ───────────────────────────────────────────────────────────

    @Test
    void refresh_fails_with_revoked_token() {
        RefreshToken revoked = RefreshToken.builder()
                .id(UUID.randomUUID()).userId(UUID.randomUUID()).token("tok")
                .expiresAt(Instant.now().plusSeconds(3600)).revoked(true).build();
        when(refreshTokenRepository.findByToken("tok")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> authService.refresh("tok"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void refresh_fails_with_expired_token() {
        RefreshToken expired = RefreshToken.builder()
                .id(UUID.randomUUID()).userId(UUID.randomUUID()).token("tok")
                .expiresAt(Instant.now().minusSeconds(1)).revoked(false).build();
        when(refreshTokenRepository.findByToken("tok")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh("tok"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // ── updateMe ──────────────────────────────────────────────────────────

    @Test
    void updateMe_fails_when_username_taken_by_other() {
        UUID userId = UUID.randomUUID();
        User user = buildUserWithId(userId, "u@test.com", "oldname");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("takenname", userId)).thenReturn(true);

        assertThatThrownBy(() -> authService.updateMe(userId, new UpdateProfileRequest("takenname")))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USERNAME_ALREADY_EXISTS);
    }

    @Test
    void updateMe_succeeds_with_same_username() {
        UUID userId = UUID.randomUUID();
        User user = buildUserWithId(userId, "u@test.com", "samename");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        var result = authService.updateMe(userId, new UpdateProfileRequest("samename"));

        assertThat(result.getUsername()).isEqualTo("samename");
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private User buildUser(String email, String username) {
        return buildUserWithId(UUID.randomUUID(), email, username);
    }

    private User buildUserWithId(UUID id, String email, String username) {
        return User.builder().id(id).email(email).username(username)
                .passwordHash("hashed").role(UserRole.USER)
                .authProvider(AuthProvider.LOCAL).enabled(true).build();
    }

    private RefreshToken buildRefreshToken() {
        return RefreshToken.builder().id(UUID.randomUUID()).userId(UUID.randomUUID())
                .token("refresh_token").expiresAt(Instant.now().plusSeconds(604800))
                .revoked(false).build();
    }
}
