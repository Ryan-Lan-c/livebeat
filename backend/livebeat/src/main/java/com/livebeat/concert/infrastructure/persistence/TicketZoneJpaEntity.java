package com.livebeat.concert.infrastructure.persistence;

import com.livebeat.concert.domain.model.TicketZone;
import com.livebeat.shared.persistence.TimestampedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * [concert] TicketZone JPA 實體
 *
 * 負責：對應 concert.ticket_zones 資料表；繼承 TimestampedEntity（created_at/updated_at）
 */
@Entity
@Table(name = "ticket_zones", schema = "concert")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketZoneJpaEntity extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "session_id", nullable = false, columnDefinition = "uuid")
    private UUID sessionId;

    @Column(name = "zone_code", nullable = false)
    private String zoneCode;

    @Column(name = "zone_name", nullable = false)
    private String zoneName;

    @Column(nullable = false)
    private int price;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "sold_seats", nullable = false)
    private int soldSeats;

    @Column(name = "locked_seats", nullable = false)
    private int lockedSeats;

    TicketZone toDomain() {
        return TicketZone.builder()
                .id(id).sessionId(sessionId).zoneCode(zoneCode).zoneName(zoneName)
                .price(price).totalSeats(totalSeats).soldSeats(soldSeats).lockedSeats(lockedSeats)
                .createdAt(getCreatedAt()).updatedAt(getUpdatedAt())
                .build();
    }

    static TicketZoneJpaEntity fromDomain(TicketZone zone) {
        return TicketZoneJpaEntity.builder()
                .id(zone.getId()).sessionId(zone.getSessionId())
                .zoneCode(zone.getZoneCode()).zoneName(zone.getZoneName())
                .price(zone.getPrice()).totalSeats(zone.getTotalSeats())
                .soldSeats(zone.getSoldSeats()).lockedSeats(zone.getLockedSeats())
                .build();
    }
}
