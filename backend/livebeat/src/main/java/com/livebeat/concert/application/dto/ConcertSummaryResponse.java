package com.livebeat.concert.application.dto;

import com.livebeat.concert.domain.model.Concert;
import com.livebeat.concert.domain.model.ConcertCategory;
import com.livebeat.concert.domain.model.ConcertStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * [concert] 演唱會列表摘要回應 DTO
 */
public record ConcertSummaryResponse(
        UUID id,
        String title,
        String artist,
        String venue,
        String city,
        String country,
        ConcertCategory category,
        ConcertStatus status,
        String imageUrl,
        Instant createdAt
) {
    public static ConcertSummaryResponse from(Concert concert) {
        return new ConcertSummaryResponse(
                concert.getId(), concert.getTitle(), concert.getArtist(),
                concert.getVenue(), concert.getCity(), concert.getCountry(),
                concert.getCategory(), concert.getStatus(),
                concert.getImageUrl(), concert.getCreatedAt()
        );
    }
}
