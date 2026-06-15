package com.livebeat.concert.domain.port;

import com.livebeat.concert.domain.model.TicketZone;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * [concert] 票區 Repository Port（次要埠，由 Infrastructure 實作）
 */
public interface TicketZoneRepository {
    TicketZone save(TicketZone zone);
    Optional<TicketZone> findById(UUID id);
    List<TicketZone> findBySessionId(UUID sessionId);
    /** 一次載入多個場次的票區，供詳情查詢批次取用、避免 N+1。 */
    List<TicketZone> findBySessionIdIn(List<UUID> sessionIds);
    void deleteById(UUID id);
    boolean hasActiveSales(UUID zoneId);
}
