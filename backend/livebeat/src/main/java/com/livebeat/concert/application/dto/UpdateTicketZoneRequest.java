package com.livebeat.concert.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * [concert] 更新票區請求 DTO（所有欄位可選，null 表示不變更）
 */
public record UpdateTicketZoneRequest(
        @Size(max = 100) String zoneName,
        @Min(0) Integer price,
        @Min(1) Integer totalSeats
) {}
