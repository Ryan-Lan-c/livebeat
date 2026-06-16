package com.livebeat.order.infrastructure.persistence;

import com.livebeat.order.domain.model.Ticket;
import com.livebeat.order.domain.port.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * [order] TicketRepository Port 的 JPA 實作（Adapter Out）
 */
@Repository
@RequiredArgsConstructor
class TicketRepositoryAdapter implements TicketRepository {

    private final TicketJpaRepository jpa;

    @Override
    public List<Ticket> saveAll(List<Ticket> tickets) {
        return jpa.saveAll(tickets.stream().map(TicketJpaEntity::fromDomain).toList()).stream()
                .map(TicketJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Ticket> findByOrderItemId(UUID orderItemId) {
        return jpa.findByOrderItemId(orderItemId).stream()
                .map(TicketJpaEntity::toDomain)
                .toList();
    }
}
