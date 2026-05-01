package com.livebeat.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * [auth] Email 登入請求 DTO
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {}
