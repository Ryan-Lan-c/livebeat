package com.livebeat.order.application.service;

import com.livebeat.concert.ConcertQueryApi;
import com.livebeat.concert.OrderableZone;
import com.livebeat.order.api.dto.CreateOrderRequest;
import com.livebeat.order.application.dto.OrderResponse;
import com.livebeat.order.domain.model.Order;
import com.livebeat.order.domain.model.OrderItem;
import com.livebeat.order.domain.port.InventoryPort;
import com.livebeat.order.domain.port.OrderRepository;
import com.livebeat.shared.exception.ApiException;
import com.livebeat.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * [order] 訂單應用服務
 *
 * 負責：v1 同步落地下單（Redis 原子扣減 → 同步寫 PG → 回傳）、冪等去重、失敗補償回補庫存、
 *       訂單查詢（以 userId fail-closed 隔離）。設計見 docs/10-order-design.md §4-1。
 * 依賴：OrderRepository, InventoryPort, ConcertQueryApi
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

    private static String generateOrderNo() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase(Locale.ROOT);
    }
}
