package com.livebeat.order.application.service;

import com.livebeat.order.domain.model.Order;
import com.livebeat.order.domain.model.OrderItem;
import com.livebeat.order.domain.model.OrderStatus;
import com.livebeat.order.domain.port.InventoryPort;
import com.livebeat.order.domain.port.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * [order] 過期訂單回收服務
 *
 * 負責：將逾時未付款的 PENDING 訂單標記 CANCELLED 並回補 Redis 庫存（見 docs/10-order-design.md §4-1）。
 *       先改狀態再回補：即使回補失敗，訂單已非 PENDING 不會被重複回收，殘留偏移由對帳 job 自癒。
 */
@Service
@RequiredArgsConstructor
public class OrderExpiryService {

    private static final Logger log = LoggerFactory.getLogger(OrderExpiryService.class);

    private final OrderRepository orderRepository;
    private final InventoryPort inventory;

    @Transactional
    public int expireOverdueOrders(Instant now) {
        List<Order> expired = orderRepository.findExpiredPending(now);
        for (Order order : expired) {
            orderRepository.updateStatus(order.getId(), OrderStatus.CANCELLED);
            for (OrderItem item : order.getItems()) {
                inventory.release(item.getZoneId(), item.getQuantity());
            }
        }
        if (!expired.isEmpty()) {
            log.info("Expired {} overdue PENDING orders and released their reserved seats", expired.size());
        }
        return expired.size();
    }
}
