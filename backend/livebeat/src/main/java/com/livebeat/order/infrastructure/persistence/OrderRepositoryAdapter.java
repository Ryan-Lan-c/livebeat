package com.livebeat.order.infrastructure.persistence;

import com.livebeat.order.domain.model.Order;
import com.livebeat.order.domain.model.OrderItem;
import com.livebeat.order.domain.model.OrderStatus;
import com.livebeat.order.domain.port.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * [order] OrderRepository Port 的 JPA 實作（Adapter Out）
 *
 * 負責：訂單與明細的存取；save() 僅處理新建（明細隨訂單一併寫入）
 */
@Repository
@RequiredArgsConstructor
class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository orderJpa;
    private final OrderItemJpaRepository itemJpa;

    @Override
    public Order save(Order order) {
        OrderJpaEntity savedOrder = orderJpa.save(OrderJpaEntity.fromDomain(order));
        List<OrderItemJpaEntity> itemEntities = order.getItems().stream()
                .map(item -> OrderItemJpaEntity.fromDomain(item, savedOrder.getId()))
                .toList();
        List<OrderItem> savedItems = itemJpa.saveAll(itemEntities).stream()
                .map(OrderItemJpaEntity::toDomain)
                .toList();
        return savedOrder.toDomain(savedItems);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return orderJpa.findById(orderId).map(this::assemble);
    }

    @Override
    public Optional<Order> findByIdempotencyKey(String idempotencyKey) {
        return orderJpa.findByIdempotencyKey(idempotencyKey).map(this::assemble);
    }

    @Override
    public List<Order> findExpiredPending(Instant now) {
        return orderJpa.findByStatusAndExpiresAtBefore(OrderStatus.PENDING, now).stream()
                .map(this::assemble)
                .toList();
    }

    @Override
    public void updateStatus(UUID orderId, OrderStatus status) {
        orderJpa.findById(orderId).ifPresent(entity -> {
            entity.setStatus(status);
            orderJpa.save(entity);
        });
    }

    @Override
    public int sumActivePendingQuantity(UUID zoneId, Instant now) {
        return itemJpa.sumActiveQuantity(zoneId, OrderStatus.PENDING, now);
    }

    private Order assemble(OrderJpaEntity entity) {
        List<OrderItem> items = itemJpa.findByOrderId(entity.getId()).stream()
                .map(OrderItemJpaEntity::toDomain)
                .toList();
        return entity.toDomain(items);
    }
}
