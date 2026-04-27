package com.livebeat.concert.infrastructure.persistence;

import com.livebeat.concert.domain.model.ConcertSession;
import com.livebeat.concert.domain.port.ConcertSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * [concert] ConcertSessionRepository Port 的 JPA 實作（Adapter Out）
 */
@Repository
@RequiredArgsConstructor
class ConcertSessionRepositoryAdapter implements ConcertSessionRepository {

    private final ConcertSessionJpaRepository jpa;

    @Override
    public ConcertSession save(ConcertSession session) {
        if (session.getId() != null) {
            return jpa.findById(session.getId())
                    .map(existing -> {
                        existing.setSessionName(session.getSessionName());
                        existing.setEventDate(session.getEventDate());
                        existing.setStatus(session.getStatus());
                        existing.setMaxTicketsPerOrder(session.getMaxTicketsPerOrder());
                        existing.setSaleStartAt(session.getSaleStartAt());
                        existing.setSaleEndAt(session.getSaleEndAt());
                        return jpa.save(existing).toDomain();
                    })
                    .orElseGet(() -> jpa.save(ConcertSessionJpaEntity.fromDomain(session)).toDomain());
        }
        return jpa.save(ConcertSessionJpaEntity.fromDomain(session)).toDomain();
    }

    @Override
    public Optional<ConcertSession> findById(UUID id) {
        return jpa.findById(id).map(ConcertSessionJpaEntity::toDomain);
    }

    @Override
    public List<ConcertSession> findByConcertId(UUID concertId) {
        return jpa.findByConcertIdOrderByEventDateAsc(concertId).stream()
                .map(ConcertSessionJpaEntity::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
