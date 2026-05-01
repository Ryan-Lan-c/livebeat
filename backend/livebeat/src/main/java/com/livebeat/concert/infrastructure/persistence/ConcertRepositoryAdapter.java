package com.livebeat.concert.infrastructure.persistence;

import com.livebeat.concert.domain.model.Concert;
import com.livebeat.concert.domain.port.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * [concert] ConcertRepository Port 的 JPA 實作（Adapter Out）
 *
 * 負責：銜接 domain port 與 Spring Data JPA，轉換 JPA 實體與 domain model
 */
@Repository
@RequiredArgsConstructor
class ConcertRepositoryAdapter implements ConcertRepository {

    private final ConcertJpaRepository jpa;

    @Override
    public Concert save(Concert concert) {
        if (concert.getId() != null) {
            return jpa.findById(concert.getId())
                    .map(existing -> {
                        existing.setTitle(concert.getTitle());
                        existing.setArtist(concert.getArtist());
                        existing.setDescription(concert.getDescription());
                        existing.setVenue(concert.getVenue());
                        existing.setCity(concert.getCity());
                        existing.setCountry(concert.getCountry());
                        existing.setCategory(concert.getCategory());
                        existing.setStatus(concert.getStatus());
                        existing.setImageUrl(concert.getImageUrl());
                        existing.setOrganizerId(concert.getOrganizerId());
                        existing.setCancelledAt(concert.getCancelledAt());
                        existing.setEndedAt(concert.getEndedAt());
                        return jpa.save(existing).toDomain();
                    })
                    .orElseGet(() -> jpa.save(ConcertJpaEntity.fromDomain(concert)).toDomain());
        }
        return jpa.save(ConcertJpaEntity.fromDomain(concert)).toDomain();
    }

    @Override
    public Optional<Concert> findById(UUID id) {
        return jpa.findById(id).map(ConcertJpaEntity::toDomain);
    }

    @Override
    public Page<Concert> searchPublic(String keyword, String category, String city, Instant cutoffTime, Pageable pageable) {
        return jpa.findPublic(cutoffTime, keyword, category, city, pageable)
                .map(ConcertJpaEntity::toDomain);
    }

    @Override
    public Page<Concert> searchAdmin(String keyword, String category, String city, UUID organizerId, Pageable pageable) {
        return jpa.findForAdmin(keyword, category, city, organizerId, pageable)
                .map(ConcertJpaEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpa.existsById(id);
    }
}
