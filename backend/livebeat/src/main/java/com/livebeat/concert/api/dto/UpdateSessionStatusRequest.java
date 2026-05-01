package com.livebeat.concert.api.dto;

import com.livebeat.concert.domain.model.SessionStatus;
import jakarta.validation.constraints.NotNull;

/**
 * [concert] 更新場次狀態請求 DTO
 */
public record UpdateSessionStatusRequest(@NotNull SessionStatus status) {}
