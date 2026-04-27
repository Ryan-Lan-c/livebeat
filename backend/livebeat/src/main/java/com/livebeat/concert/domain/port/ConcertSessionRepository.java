package com.livebeat.concert.domain.port;

import com.livebeat.concert.domain.model.ConcertSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * [concert] 演唱會場次 Repository Port（次要埠，由 Infrastructure 實作）
 */
public interface ConcertSessionRepository {
    ConcertSession save(ConcertSession session);
    Optional<ConcertSession> findById(UUID id);
    List<ConcertSession> findByConcertId(UUID concertId);
    void deleteById(UUID id);
}
