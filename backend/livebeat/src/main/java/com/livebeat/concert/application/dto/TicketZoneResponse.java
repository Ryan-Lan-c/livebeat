package com.livebeat.concert.application.dto;

import com.livebeat.concert.domain.model.TicketZone;

import java.util.UUID;

/**
 * [concert] 票區回應 DTO
 */
public record TicketZoneResponse(
        UUID id,
        UUID sessionId,
        String zoneCode,
        String zoneName,
        int price,
        int totalSeats,
        int soldSeats,
        int lockedSeats,
        int availableSeats
) {
    public static TicketZoneResponse from(TicketZone zone) {
        return new TicketZoneResponse(
                zone.getId(), zone.getSessionId(),
                zone.getZoneCode(), zone.getZoneName(),
                zone.getPrice(), zone.getTotalSeats(),
                zone.getSoldSeats(), zone.getLockedSeats(),
                zone.availableSeats()
        );
    }
}
