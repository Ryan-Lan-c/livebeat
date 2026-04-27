package com.livebeat.concert.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

/**
 * [concert] 演唱會場次領域模型（純 Java，無框架依賴）
 *
 * 負責：封裝場次核心屬性，包含售票時間、座位模式與每單上限
 */
@Getter
@Builder
@With
public class ConcertSession {
    private final UUID id;
    private final UUID concertId;
    private final String sessionName;
    private final Instant eventDate;
    private final SessionStatus status;
    private final boolean hasAssignedSeats;
    private final int maxTicketsPerOrder;
    private final Instant saleStartAt;
    private final Instant saleEndAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    public static ConcertSession create(UUID concertId, String sessionName, Instant eventDate,
                                         boolean hasAssignedSeats, int maxTicketsPerOrder,
                                         Instant saleStartAt, Instant saleEndAt) {
        return ConcertSession.builder()
                .concertId(concertId).sessionName(sessionName).eventDate(eventDate)
                .status(SessionStatus.DRAFT)
                .hasAssignedSeats(hasAssignedSeats).maxTicketsPerOrder(maxTicketsPerOrder)
                .saleStartAt(saleStartAt).saleEndAt(saleEndAt)
                .build();
    }
}
