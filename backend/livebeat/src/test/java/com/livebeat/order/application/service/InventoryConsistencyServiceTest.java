package com.livebeat.order.application.service;

import com.livebeat.concert.ConcertQueryApi;
import com.livebeat.concert.ZoneInventorySnapshot;
import com.livebeat.order.domain.port.InventoryPort;
import com.livebeat.order.domain.port.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * [order] InventoryConsistencyService 單元測試：warm-up 與對帳/復原各分支。
 */
@ExtendWith(MockitoExtension.class)
class InventoryConsistencyServiceTest {

    @Mock ConcertQueryApi concertQuery;
    @Mock OrderRepository orderRepository;
    @Mock InventoryPort inventory;
    @InjectMocks InventoryConsistencyService service;

    private final UUID zoneId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();

    private ZoneInventorySnapshot snapshot(int total, int sold) {
        return new ZoneInventorySnapshot(zoneId, sessionId, total, sold);
    }

    @Test
    void warmUpSession_seeds_total_minus_sold() {
        when(concertQuery.zonesForSession(sessionId)).thenReturn(List.of(snapshot(100, 10)));

        service.warmUpSession(sessionId);

        verify(inventory).warmUp(zoneId, 90);
    }

    @Test
    void reconcile_rebuilds_when_redis_missing() {
        Instant now = Instant.now();
        when(concertQuery.onSaleZones()).thenReturn(List.of(snapshot(100, 0)));
        when(orderRepository.sumActivePendingQuantity(zoneId, now)).thenReturn(20);
        when(inventory.remaining(zoneId)).thenReturn(null);

        assertThat(service.reconcileAll(now)).isEqualTo(1);
        verify(inventory).warmUp(zoneId, 80);
    }

    @Test
    void reconcile_corrects_drift() {
        Instant now = Instant.now();
        when(concertQuery.onSaleZones()).thenReturn(List.of(snapshot(100, 0)));
        when(orderRepository.sumActivePendingQuantity(zoneId, now)).thenReturn(10);
        when(inventory.remaining(zoneId)).thenReturn(50L);

        assertThat(service.reconcileAll(now)).isEqualTo(1);
        verify(inventory).warmUp(zoneId, 90);
    }

    @Test
    void reconcile_is_noop_when_consistent() {
        Instant now = Instant.now();
        when(concertQuery.onSaleZones()).thenReturn(List.of(snapshot(100, 0)));
        when(orderRepository.sumActivePendingQuantity(zoneId, now)).thenReturn(10);
        when(inventory.remaining(zoneId)).thenReturn(90L);

        assertThat(service.reconcileAll(now)).isZero();
        verify(inventory, never()).warmUp(any(), anyInt());
    }
}
