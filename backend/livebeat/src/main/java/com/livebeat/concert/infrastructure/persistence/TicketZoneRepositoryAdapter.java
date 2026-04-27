package com.livebeat.concert.infrastructure.persistence;

import com.livebeat.concert.domain.model.TicketZone;
import com.livebeat.concert.domain.port.TicketZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * [concert] TicketZoneRepository Port 的 JPA 實作（Adapter Out）
 */
@Repository
@RequiredArgsConstructor
class TicketZoneRepositoryAdapter implements TicketZoneRepository {

    private final TicketZoneJpaRepository jpa;

    @Override
    public TicketZone save(TicketZone zone) {
        if (zone.getId() != null) {
            return jpa.findById(zone.getId())
                    .map(existing -> {
                        existing.setZoneName(zone.getZoneName());
                        existing.setPrice(zone.getPrice());
                        existing.setTotalSeats(zone.getTotalSeats());
                        existing.setSoldSeats(zone.getSoldSeats());
                        existing.setLockedSeats(zone.getLockedSeats());
                        return jpa.save(existing).toDomain();
                    })
                    .orElseGet(() -> jpa.save(TicketZoneJpaEntity.fromDomain(zone)).toDomain());
        }
        return jpa.save(TicketZoneJpaEntity.fromDomain(zone)).toDomain();
    }

    @Override
    public Optional<TicketZone> findById(UUID id) {
        return jpa.findById(id).map(TicketZoneJpaEntity::toDomain);
    }

    @Override
    public List<TicketZone> findBySessionId(UUID sessionId) {
        return jpa.findBySessionIdOrderByZoneCodeAsc(sessionId).stream()
                .map(TicketZoneJpaEntity::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public boolean hasActiveSales(UUID zoneId) {
        return jpa.hasActiveSales(zoneId);
    }
}
