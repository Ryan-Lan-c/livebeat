package com.livebeat.concert.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * [concert] 建立票區請求 DTO
 */
public record CreateTicketZoneRequest(
        @NotBlank @Size(max = 20) String zoneCode,
        @NotBlank @Size(max = 100) String zoneName,
        @Min(0) int price,
        @Min(1) int totalSeats
) {}
