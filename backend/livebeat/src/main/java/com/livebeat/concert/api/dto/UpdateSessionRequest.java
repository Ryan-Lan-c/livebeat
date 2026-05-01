package com.livebeat.concert.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * [concert] 更新場次請求 DTO（所有欄位可選，null 表示不變更）
 */
public record UpdateSessionRequest(
        @Size(max = 100) String sessionName,
        Instant eventDate,
        @Min(1) Integer maxTicketsPerOrder,
        Instant saleStartAt,
        Instant saleEndAt
) {}
