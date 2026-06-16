package com.livebeat.order.infrastructure.persistence;

import com.livebeat.order.domain.model.Ticket;
import com.livebeat.order.domain.model.TicketStatus;
import com.livebeat.shared.persistence.CreatedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * [order] Ticket JPA 實體
 *
 * 負責：對應 "order".tickets 資料表；繼承 CreatedEntity（created_at）。seat_id 區域票為 null。
 */
@Entity
@Table(name = "tickets", schema = "`order`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketJpaEntity extends CreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "order_item_id", nullable = false, columnDefinition = "uuid")
    private UUID orderItemId;

    @Column(name = "seat_id", columnDefinition = "uuid")
    private UUID seatId;

    @Column(name = "ticket_code", nullable = false)
    private String ticketCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(name = "used_at")
    private Instant usedAt;

    Ticket toDomain() {
        return Ticket.builder()
                .id(id).orderItemId(orderItemId).seatId(seatId).ticketCode(ticketCode)
                .status(status).usedAt(usedAt).createdAt(getCreatedAt())
                .build();
    }

    static TicketJpaEntity fromDomain(Ticket ticket) {
        return TicketJpaEntity.builder()
                .id(ticket.getId()).orderItemId(ticket.getOrderItemId()).seatId(ticket.getSeatId())
                .ticketCode(ticket.getTicketCode()).status(ticket.getStatus()).usedAt(ticket.getUsedAt())
                .build();
    }
}
