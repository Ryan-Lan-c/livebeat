package com.livebeat.concert.application.dto;

import com.livebeat.concert.domain.model.ConcertStatus;
import jakarta.validation.constraints.NotNull;

/**
 * [concert] 更新演唱會狀態請求 DTO
 */
public record UpdateStatusRequest(@NotNull ConcertStatus status) {}
