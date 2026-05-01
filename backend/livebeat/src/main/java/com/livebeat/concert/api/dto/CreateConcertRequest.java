package com.livebeat.concert.api.dto;

import com.livebeat.concert.domain.model.ConcertCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * [concert] 建立演唱會請求 DTO
 *
 * organizerId 選填：ORGANIZER 不傳時預設為自己；ADMIN 可指定其他主辦方 UUID
 */
public record CreateConcertRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String artist,
        String description,
        @NotBlank @Size(max = 255) String venue,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 100) String country,
        @NotNull ConcertCategory category,
        UUID organizerId
) {}
