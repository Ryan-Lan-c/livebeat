package com.livebeat.concert.infrastructure.persistence;

import com.livebeat.concert.domain.model.ConcertSession;
import com.livebeat.concert.domain.model.SessionStatus;
import com.livebeat.shared.persistence.TimestampedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * [concert] ConcertSession JPA 實體
 *
 * 負責：對應 concert.concert_sessions 資料表；繼承 TimestampedEntity（created_at/updated_at）
 */
@Entity
@Table(name = "concert_sessions", schema = "concert")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcertSessionJpaEntity extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "concert_id", nullable = false, columnDefinition = "uuid")
    private UUID concertId;

    @Column(name = "session_name", nullable = false)
    private String sessionName;

    @Column(name = "event_date", nullable = false)
    private Instant eventDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(name = "has_assigned_seats", nullable = false)
    private boolean hasAssignedSeats;

    @Column(name = "max_tickets_per_order", nullable = false)
    private int maxTicketsPerOrder;

    @Column(name = "sale_start_at")
    private Instant saleStartAt;

    @Column(name = "sale_end_at")
    private Instant saleEndAt;

    ConcertSession toDomain() {
        return ConcertSession.builder()
                .id(id).concertId(concertId).sessionName(sessionName)
                .eventDate(eventDate).status(status)
                .hasAssignedSeats(hasAssignedSeats).maxTicketsPerOrder(maxTicketsPerOrder)
                .saleStartAt(saleStartAt).saleEndAt(saleEndAt)
                .createdAt(getCreatedAt()).updatedAt(getUpdatedAt())
                .build();
    }

    static ConcertSessionJpaEntity fromDomain(ConcertSession session) {
        return ConcertSessionJpaEntity.builder()
                .id(session.getId()).concertId(session.getConcertId())
                .sessionName(session.getSessionName()).eventDate(session.getEventDate())
                .status(session.getStatus())
                .hasAssignedSeats(session.isHasAssignedSeats())
                .maxTicketsPerOrder(session.getMaxTicketsPerOrder())
                .saleStartAt(session.getSaleStartAt()).saleEndAt(session.getSaleEndAt())
                .build();
    }
}
