package com.livebeat.concert.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * [concert] Concert Spring Data JPA Repository
 */
interface ConcertJpaRepository extends JpaRepository<ConcertJpaEntity, UUID> {

    /**
     * 公開演唱會搜尋（PUBLISHED / ON_SALE）。
     * keyword 已由 service 預格式化為 %keyword%（lowercase），null 表示不做關鍵字篩選。
     */
    @Query("""
            SELECT c FROM ConcertJpaEntity c
            WHERE c.status IN :statuses
            AND (:keyword IS NULL
                 OR LOWER(c.title) LIKE :keyword
                 OR LOWER(c.artist) LIKE :keyword
                 OR LOWER(c.venue) LIKE :keyword)
            AND (:category IS NULL OR c.category = :category)
            AND (:city IS NULL OR c.city = :city)
            """)
    Page<ConcertJpaEntity> findPublic(
            @Param("statuses") List<String> statuses,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("city") String city,
            Pageable pageable);
}
