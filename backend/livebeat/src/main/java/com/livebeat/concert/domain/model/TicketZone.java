package com.livebeat.concert.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.util.UUID;

/**
 * [concert] 票區領域模型（純 Java，無框架依賴）
 *
 * 負責：封裝票區屬性與庫存計算；availableSeats() 為即時可購票數
 */
@Getter
@Builder
@With
public class TicketZone {
    private final UUID id;
    private final UUID sessionId;
    private final String zoneCode;
    private final String zoneName;
    private final int price;
    private final int totalSeats;
    private final int soldSeats;
    private final int lockedSeats;
    private final Instant createdAt;
    private final Instant updatedAt;

    public int availableSeats() {
        return totalSeats - soldSeats - lockedSeats;
    }

    public boolean hasSoldOrLockedTickets() {
        return soldSeats > 0 || lockedSeats > 0;
    }

    public static TicketZone create(UUID sessionId, String zoneCode, String zoneName,
                                     int price, int totalSeats) {
        return TicketZone.builder()
                .sessionId(sessionId).zoneCode(zoneCode).zoneName(zoneName)
                .price(price).totalSeats(totalSeats)
                .soldSeats(0).lockedSeats(0)
                .build();
    }
}
