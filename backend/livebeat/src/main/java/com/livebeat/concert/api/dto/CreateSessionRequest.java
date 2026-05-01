package com.livebeat.concert.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * [concert] 建立場次請求 DTO
 */
public record CreateSessionRequest(
        @NotBlank @Size(max = 100) String sessionName,
        @NotNull Instant eventDate,
        boolean hasAssignedSeats,
        @Min(1) int maxTicketsPerOrder,
        Instant saleStartAt,
        Instant saleEndAt
) {}
