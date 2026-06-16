package com.livebeat.order.domain.port;

import com.livebeat.order.domain.model.Ticket;

import java.util.List;
import java.util.UUID;

/**
 * [order] 票券資料存取介面（Port）
 */
public interface TicketRepository {
    List<Ticket> saveAll(List<Ticket> tickets);

    List<Ticket> findByOrderItemId(UUID orderItemId);
}
