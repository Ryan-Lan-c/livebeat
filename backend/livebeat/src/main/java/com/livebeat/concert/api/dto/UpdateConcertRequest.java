package com.livebeat.concert.api.dto;

import com.livebeat.concert.domain.model.ConcertCategory;
import jakarta.validation.constraints.Size;

/**
 * [concert] 更新演唱會請求 DTO（所有欄位可選，null 表示不變更）
 */
public record UpdateConcertRequest(
        @Size(max = 255) String title,
        @Size(max = 255) String artist,
        String description,
        @Size(max = 255) String venue,
        @Size(max = 100) String city,
        @Size(max = 100) String country,
        ConcertCategory category
) {}
