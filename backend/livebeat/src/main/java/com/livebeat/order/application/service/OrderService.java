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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * [order] 訂單應用服務
 *
 * 負責：v1 同步落地下單（Redis 原子扣減 → 同步寫 PG → 回傳）、冪等去重、失敗補償回補庫存、
 *       訂單查詢（以 userId fail-closed 隔離）、sandbox 付款（PENDING→PAID、sold_seats++、出票）。
 *       設計見 docs/10-order-design.md §4-1。
 * 依賴：OrderRepository, InventoryPort, ConcertQueryApi, ConcertSaleApi, TicketRepository
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String DEFAULT_CURRENCY = "TWD";
    private static final Duration ORDER_LOCK_TTL = Duration.ofMinutes(10);

    private final OrderRepository orderRepository;
    private final InventoryPort inventory;
    private final ConcertQueryApi concertQuery;
    private final ConcertSaleApi concertSale;
    private final TicketRepository ticketRepository;

    public OrderResponse createOrder(CreateOrderRequest request, UUID userId) {
        // 冪等：相同 key 已建立過則回既有訂單，不重複扣減
        if (request.idempotencyKey() != null) {
            Optional<Order> existing = orderRepository.findByIdempotencyKey(request.idempotencyKey());
            if (existing.isPresent()) {
                return OrderResponse.from(existing.get());
            }
        }

        OrderableZone zone = concertQuery.findOrderableZone(request.sessionId(), request.zoneId())
                .orElseThrow(() -> new ApiException(ErrorCode.ZONE_NOT_FOUND));
        if (!zone.saleOpen()) {
            throw new ApiException(ErrorCode.SESSION_SALE_NOT_OPEN);
        }
        if (request.quantity() > zone.maxTicketsPerOrder()) {
            throw new ApiException(ErrorCode.EXCEEDS_MAX_PER_ORDER);
        }

        InventoryPort.Reservation reservation = inventory.tryReserve(request.zoneId(), request.quantity());
        switch (reservation) {
            case NOT_READY -> throw new ApiException(ErrorCode.INVENTORY_NOT_READY);
            case SOLD_OUT -> throw new ApiException(ErrorCode.SEATS_SOLD_OUT);
            case RESERVED -> { /* 已扣減，繼續同步落地 */ }
        }

        try {
            OrderItem item = OrderItem.of(request.zoneId(), request.quantity(), zone.price());
            Order order = Order.create(userId, request.sessionId(), generateOrderNo(),
                    DEFAULT_CURRENCY, request.idempotencyKey(),
                    Instant.now().plus(ORDER_LOCK_TTL), List.of(item));
            return OrderResponse.from(orderRepository.save(order));
        } catch (RuntimeException e) {
            // 同步落地失敗：補償回補已扣減的庫存，避免 Redis 少算（見 docs/10-order-design.md §4-1）
            inventory.release(request.zoneId(), request.quantity());
            log.warn("Order persistence failed for user {} zone {}; released {} reserved seats",
                    userId, request.zoneId(), request.quantity());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getUserId().equals(userId))
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));
        return OrderResponse.from(order);
    }

    /**
     * Sandbox 付款：將 PENDING 訂單確認為 PAID、票區 sold_seats++、出票。無真實金流。
     * 真實金流（綠界 / 藍新 / Stripe）與電子發票屬 payment 模組後續工作。
     */
    public OrderResponse payOrder(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getUserId().equals(userId))
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));
        boolean payable = order.getStatus() == OrderStatus.PENDING
                && (order.getExpiresAt() == null || order.getExpiresAt().isAfter(Instant.now()));
        if (!payable) {
            throw new ApiException(ErrorCode.ORDER_NOT_PAYABLE);
        }
        // 確認售出：sold_seats++，使該訂單脫離 pending 集合後仍維持
        // remaining = total - sold - 進行中鎖定 不變式（否則對帳會把已售座位當 drift 釋放）
        for (OrderItem item : order.getItems()) {
            concertSale.confirmSale(item.getZoneId(), item.getQuantity());
        }
        orderRepository.updateStatus(orderId, OrderStatus.PAID);
        // 出票：每張一筆 ticket（區域票 seatId = null）
        List<Ticket> tickets = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                tickets.add(Ticket.issue(item.getId(), null, generateTicketCode()));
            }
        }
        ticketRepository.saveAll(tickets);
        log.info("Order {} paid; issued {} tickets", orderId, tickets.size());
        return OrderResponse.from(order.withStatus(OrderStatus.PAID));
    }

    private static String generateOrderNo() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private static String generateTicketCode() {
        return UUID.randomUUID().toString();
    }
}
