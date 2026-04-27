package com.livebeat.concert.application.dto;

import com.livebeat.concert.domain.model.ConcertCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * [concert] 建立演唱會請求 DTO
 */
public record CreateConcertRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String artist,
        String description,
        @NotBlank @Size(max = 255) String venue,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 100) String country,
        @NotNull ConcertCategory category
) {}
