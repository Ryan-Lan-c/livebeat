package com.livebeat.concert.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

/**
 * [concert] 演唱會領域模型（純 Java，無框架依賴）
 *
 * 負責：封裝演唱會核心屬性；透過 Concert.create() 建立新演唱會（預設 DRAFT 狀態）
 */
@Getter
@Builder
@With
public class Concert {
    private final UUID id;
    private final String title;
    private final String artist;
    private final String description;
    private final String venue;
    private final String city;
    private final String country;
    private final ConcertCategory category;
    private final ConcertStatus status;
    private final String imageUrl;
    private final UUID organizerId;
    private final UUID createdBy;
    private final UUID updatedBy;
    private final Instant createdAt;
    private final Instant updatedAt;

    public static Concert create(String title, String artist, String description,
                                  String venue, String city, String country,
                                  ConcertCategory category, UUID organizerId) {
        return Concert.builder()
                .title(title).artist(artist).description(description)
                .venue(venue).city(city).country(country)
                .category(category).status(ConcertStatus.DRAFT)
                .organizerId(organizerId)
                .build();
    }
}
