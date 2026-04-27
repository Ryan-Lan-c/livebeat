package com.livebeat.concert.application.dto;

import com.livebeat.concert.domain.model.Concert;
import com.livebeat.concert.domain.model.ConcertCategory;
import com.livebeat.concert.domain.model.ConcertStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * [concert] 演唱會詳情回應 DTO（含場次列表）
 */
public record ConcertDetailResponse(
        UUID id,
        String title,
        String artist,
        String description,
        String venue,
        String city,
        String country,
        ConcertCategory category,
        ConcertStatus status,
        String imageUrl,
        UUID organizerId,
        List<ConcertSessionResponse> sessions,
        Instant createdAt,
        Instant updatedAt
) {
    public static ConcertDetailResponse from(Concert concert, List<ConcertSessionResponse> sessions) {
        return new ConcertDetailResponse(
                concert.getId(), concert.getTitle(), concert.getArtist(),
                concert.getDescription(), concert.getVenue(),
                concert.getCity(), concert.getCountry(),
                concert.getCategory(), concert.getStatus(),
                concert.getImageUrl(), concert.getOrganizerId(),
                sessions, concert.getCreatedAt(), concert.getUpdatedAt()
        );
    }
}
