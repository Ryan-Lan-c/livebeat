package com.livebeat.order.application.service;

import com.livebeat.concert.ConcertQueryApi;
import com.livebeat.concert.ConcertSaleApi;
import com.livebeat.concert.OrderableZone;
import com.livebeat.order.api.dto.CreateOrderRequest;
import com.livebeat.order.application.dto.OrderResponse;
import com.livebeat.order.domain.model.Order;
import com.livebeat.order.domain.model.OrderItem;
import com.livebeat.order.domain.model.OrderStatus;
import com.livebeat.order.domain.model.Ticket;
import com.livebeat.order.domain.port.InventoryPort;
import com.livebeat.order.domain.port.OrderRepository;
import com.livebeat.order.domain.port.TicketRepository;
import com.livebeat.shared.exception.ApiException;
import com.livebeat.shared.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * [order] OrderService 單元測試
 *
 * 涵蓋：售票未開放 / 超量 / 售罄 / 未就緒的拒絕、成功落地、落地失敗的庫存補償、冪等去重。
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock InventoryPort inventory;
    @Mock ConcertQueryApi concertQuery;
    @Mock ConcertSaleApi concertSale;
    @Mock TicketRepository ticketRepository;
    @InjectMocks OrderService orderService;

    private final UUID userId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();
    private final UUID zoneId = UUID.randomUUID();

    private CreateOrderRequest request(int quantity, String key) {
        return new CreateOrderRequest(sessionId, zoneId, quantity, key);
    }

    private OrderableZone zone(boolean saleOpen, int maxPerOrder) {
        return new OrderableZone(zoneId, sessionId, 1000, saleOpen, maxPerOrder);
    }

    @Test
    void reserves_and_persists_on_success() {
        when(concertQuery.findOrderableZone(sessionId, zoneId)).thenReturn(Optional.of(zone(true, 4)));
        when(inventory.tryReserve(zoneId, 2)).thenReturn(InventoryPort.Reservation.RESERVED);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.createOrder(request(2, null), userId);

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.totalAmount()).isEqualTo(2000);
        verify(inventory, never()).release(any(), anyInt());
    }

    @Test
    void rejects_unknown_zone() {
        when(concertQuery.findOrderableZone(sessionId, zoneId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request(2, null), userId))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(ErrorCode.ZONE_NOT_FOUND);
        verify(inventory, never()).tryReserve(any(), anyInt());
    }

    @Test
    void rejects_when_sale_not_open() {
        when(concertQuery.findOrderableZone(sessionId, zoneId)).thenReturn(Optional.of(zone(false, 4)));

        assertThatThrownBy(() -> orderService.createOrder(request(2, null), userId))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(ErrorCode.SESSION_SALE_NOT_OPEN);
        verify(inventory, never()).tryReserve(any(), anyInt());
    }

    @Test
    void rejects_when_quantity_exceeds_max() {
        when(concertQuery.findOrderableZone(sessionId, zoneId)).thenReturn(Optional.of(zone(true, 1)));

        assertThatThrownBy(() -> orderService.createOrder(request(2, null), userId))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(ErrorCode.EXCEEDS_MAX_PER_ORDER);
        verify(inventory, never()).tryReserve(any(), anyInt());
    }

    @Test
    void maps_sold_out_to_error_and_does_not_persist() {
        when(concertQuery.findOrderableZone(sessionId, zoneId)).thenReturn(Optional.of(zone(true, 4)));
        when(inventory.tryReserve(zoneId, 2)).thenReturn(InventoryPort.Reservation.SOLD_OUT);

        assertThatThrownBy(() -> orderService.createOrder(request(2, null), userId))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(ErrorCode.SEATS_SOLD_OUT);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void maps_not_ready_to_error() {
        when(concertQuery.findOrderableZone(sessionId, zoneId)).thenReturn(Optional.of(zone(true, 4)));
        when(inventory.tryReserve(zoneId, 2)).thenReturn(InventoryPort.Reservation.NOT_READY);

        assertThatThrownBy(() -> orderService.createOrder(request(2, null), userId))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVENTORY_NOT_READY);
    }

    @Test
    void releases_inventory_when_persistence_fails() {
        when(concertQuery.findOrderableZone(sessionId, zoneId)).thenReturn(Optional.of(zone(true, 4)));
        when(inventory.tryReserve(zoneId, 2)).thenReturn(InventoryPort.Reservation.RESERVED);
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> orderService.createOrder(request(2, null), userId))
                .isInstanceOf(RuntimeException.class);
        verify(inventory).release(zoneId, 2);
    }

    @Test
    void is_idempotent_for_known_key() {
        Order existing = Order.create(userId, sessionId, "ORD-EXISTING", "TWD", "key-1",
                null, List.of(OrderItem.of(zoneId, 2, 1000)));
        when(orderRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(existing));

        OrderResponse response = orderService.createOrder(request(2, "key-1"), userId);

        assertThat(response.orderNo()).isEqualTo("ORD-EXISTING");
        verify(concertQuery, never()).findOrderableZone(any(), any());
        verify(inventory, never()).tryReserve(any(), anyInt());
    }

    @Test
    @SuppressWarnings("unchecked")
    void payOrder_confirms_sale_and_issues_tickets() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        OrderItem item = OrderItem.builder().id(itemId).zoneId(zoneId).quantity(2).unitPrice(1000).build();
        Order order = Order.create(userId, sessionId, "ORD-1", "TWD", null,
                Instant.now().plusSeconds(600), List.of(item)).withId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.payOrder(orderId, userId);

        assertThat(response.status()).isEqualTo("PAID");
        verify(concertSale).confirmSale(zoneId, 2);
        verify(orderRepository).updateStatus(orderId, OrderStatus.PAID);
        ArgumentCaptor<List<Ticket>> captor = ArgumentCaptor.forClass(List.class);
        verify(ticketRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    void payOrder_rejects_non_pending_order() {
        UUID orderId = UUID.randomUUID();
        Order paid = Order.create(userId, sessionId, "ORD-1", "TWD", null,
                        Instant.now().plusSeconds(600), List.of(OrderItem.of(zoneId, 1, 1000)))
                .withId(orderId).withStatus(OrderStatus.PAID);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(paid));

        assertThatThrownBy(() -> orderService.payOrder(orderId, userId))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_PAYABLE);
        verify(concertSale, never()).confirmSale(any(), anyInt());
        verify(orderRepository, never()).updateStatus(any(), any());
    }
}
