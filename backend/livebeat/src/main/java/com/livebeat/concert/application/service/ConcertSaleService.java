package com.livebeat.concert.application.service;

import com.livebeat.concert.ConcertSaleApi;
import com.livebeat.concert.domain.model.TicketZone;
import com.livebeat.concert.domain.port.TicketZoneRepository;
import com.livebeat.shared.exception.ApiException;
import com.livebeat.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * [concert] ConcertSaleApi 的實作（跨模組售出命令，Adapter）
 *
 * 負責：付款成功後將票區 sold_seats 增量。read-modify-write 由 ticket_zones 的 @Version
 *       樂觀鎖保護；併發確認售出時後提交者收到 OptimisticLockException（→ 409）。
 */
@Service
@RequiredArgsConstructor
@Transactional
class ConcertSaleService implements ConcertSaleApi {

    private final TicketZoneRepository zoneRepository;

    @Override
    public void confirmSale(UUID zoneId, int quantity) {
        TicketZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ApiException(ErrorCode.ZONE_NOT_FOUND));
        zoneRepository.save(zone.withSoldSeats(zone.getSoldSeats() + quantity));
    }
}
