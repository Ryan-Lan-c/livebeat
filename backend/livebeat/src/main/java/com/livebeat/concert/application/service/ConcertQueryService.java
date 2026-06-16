package com.livebeat.concert.application.service;

import com.livebeat.concert.ConcertQueryApi;
import com.livebeat.concert.OrderableZone;
import com.livebeat.concert.ZoneInventorySnapshot;
import com.livebeat.concert.domain.model.ConcertSession;
import com.livebeat.concert.domain.model.SessionStatus;
import com.livebeat.concert.domain.port.ConcertSessionRepository;
import com.livebeat.concert.domain.port.TicketZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * [concert] ConcertQueryApi 的實作（跨模組查詢，Adapter）
 *
 * 負責：以 session + zone 查詢可下單票區，並依場次狀態與售票時間窗判定 saleOpen
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ConcertQueryService implements ConcertQueryApi {

    private final ConcertSessionRepository sessionRepository;
    private final TicketZoneRepository zoneRepository;

    @Override
    public Optional<OrderableZone> findOrderableZone(UUID sessionId, UUID zoneId) {
        return zoneRepository.findById(zoneId)
                .filter(zone -> zone.getSessionId().equals(sessionId))
                .flatMap(zone -> sessionRepository.findById(sessionId)
                        .map(session -> new OrderableZone(
                                zone.getId(), sessionId, zone.getPrice(),
                                isSaleOpen(session), session.getMaxTicketsPerOrder())));
    }

    @Override
    public List<ZoneInventorySnapshot> zonesForSession(UUID sessionId) {
        return zoneRepository.findBySessionId(sessionId).stream()
                .map(zone -> new ZoneInventorySnapshot(
                        zone.getId(), zone.getSessionId(), zone.getTotalSeats(), zone.getSoldSeats()))
                .toList();
    }

    @Override
    public List<ZoneInventorySnapshot> onSaleZones() {
        return sessionRepository.findByStatus(SessionStatus.ON_SALE).stream()
                .flatMap(session -> zoneRepository.findBySessionId(session.getId()).stream())
                .map(zone -> new ZoneInventorySnapshot(
                        zone.getId(), zone.getSessionId(), zone.getTotalSeats(), zone.getSoldSeats()))
                .toList();
    }

    /** 售票開放：場次為 ON_SALE 且當下在售票時間窗內（窗為選填）。 */
    private boolean isSaleOpen(ConcertSession session) {
        if (session.getStatus() != SessionStatus.ON_SALE) {
            return false;
        }
        Instant now = Instant.now();
        if (session.getSaleStartAt() != null && now.isBefore(session.getSaleStartAt())) {
            return false;
        }
        return session.getSaleEndAt() == null || !now.isAfter(session.getSaleEndAt());
    }
}
