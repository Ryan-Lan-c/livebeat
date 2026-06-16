package com.livebeat.order.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * [order] 票券領域模型（純 Java，無框架依賴）
 *
 * 負責：付款後出票的單張票券；seatId 為 null 表示區域票；issue() 以 VALID 起始
 */
@Getter
@Builder
public class Ticket {
    private final UUID id;
    private final UUID orderItemId;
    private final UUID seatId;
    private final String ticketCode;
    private final TicketStatus status;
    private final Instant usedAt;
    private final Instant createdAt;

    public static Ticket issue(UUID orderItemId, UUID seatId, String ticketCode) {
        return Ticket.builder()
                .orderItemId(orderItemId).seatId(seatId).ticketCode(ticketCode)
                .status(TicketStatus.VALID)
                .build();
    }
}
