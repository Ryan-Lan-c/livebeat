package com.livebeat.concert.application.dto;

import com.livebeat.concert.domain.model.ConcertSession;
import com.livebeat.concert.domain.model.SessionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * [concert] 場次回應 DTO（含票區列表）
 */
public record ConcertSessionResponse(
        UUID id,
        UUID concertId,
        String sessionName,
        Instant eventDate,
        SessionStatus status,
        boolean hasAssignedSeats,
        int maxTicketsPerOrder,
        Instant saleStartAt,
        Instant saleEndAt,
        List<TicketZoneResponse> zones
) {
    public static ConcertSessionResponse from(ConcertSession session, List<TicketZoneResponse> zones) {
        return new ConcertSessionResponse(
                session.getId(), session.getConcertId(),
                session.getSessionName(), session.getEventDate(),
                session.getStatus(), session.isHasAssignedSeats(),
                session.getMaxTicketsPerOrder(),
                session.getSaleStartAt(), session.getSaleEndAt(),
                zones
        );
    }
}
