package com.livebeat.order.application.service;

import com.livebeat.order.domain.model.Order;
import com.livebeat.order.domain.model.OrderItem;
import com.livebeat.order.domain.model.OrderStatus;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * [order] OrderExpiryService 單元測試：過期訂單取消 + 庫存回補。
 */
@ExtendWith(MockitoExtension.class)
class OrderExpiryServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock InventoryPort inventory;
    @InjectMocks OrderExpiryService service;

    @Test
    void expires_pending_orders_and_releases_inventory() {
        Instant now = Instant.now();
        UUID orderId = UUID.randomUUID();
        UUID zoneId = UUID.randomUUID();
        Order order = Order.create(UUID.randomUUID(), UUID.randomUUID(), "ORD-1", "TWD", null, now,
                List.of(OrderItem.of(zoneId, 2, 1000))).withId(orderId);
        when(orderRepository.findExpiredPending(now)).thenReturn(List.of(order));

        int count = service.expireOverdueOrders(now);

        assertThat(count).isEqualTo(1);
        verify(orderRepository).updateStatus(orderId, OrderStatus.CANCELLED);
        verify(inventory).release(zoneId, 2);
    }

    @Test
    void no_expired_orders_is_noop() {
        Instant now = Instant.now();
        when(orderRepository.findExpiredPending(now)).thenReturn(List.of());

        assertThat(service.expireOverdueOrders(now)).isZero();
        verifyNoInteractions(inventory);
    }
}
