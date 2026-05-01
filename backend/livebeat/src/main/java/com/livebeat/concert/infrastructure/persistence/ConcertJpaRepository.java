package com.livebeat.concert.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

/**
 * [concert] Concert Spring Data JPA Repository
 */
interface ConcertJpaRepository extends JpaRepository<ConcertJpaEntity, UUID> {

    /**
     * 公開搜尋：PUBLISHED/ON_SALE 無條件顯示；CANCELLED/ENDED 僅顯示 cutoffTime 之後發生的。
     * keyword 已由 service 預格式化為 %keyword%（lowercase），null 表示不做關鍵字篩選。
     */
    @Query("""
            SELECT c FROM ConcertJpaEntity c
            WHERE (
                c.status IN ('PUBLISHED', 'ON_SALE')
                OR (c.status = 'CANCELLED' AND c.cancelledAt IS NOT NULL AND c.cancelledAt >= :cutoffTime)
                OR (c.status = 'ENDED'     AND c.endedAt     IS NOT NULL AND c.endedAt     >= :cutoffTime)
            )
            AND (:keyword  IS NULL OR LOWER(c.title) LIKE :keyword
                                   OR LOWER(c.artist) LIKE :keyword
                                   OR LOWER(c.venue) LIKE :keyword)
            AND (:category IS NULL OR c.category = :category)
            AND (:city     IS NULL OR c.city = :city)
            """)
    Page<ConcertJpaEntity> findPublic(
            @Param("cutoffTime") Instant cutoffTime,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("city") String city,
            Pageable pageable);

    /**
     * 後台搜尋：不過濾狀態；organizerId 不為 null 時只回傳該主辦方的演唱會。
     */
    @Query("""
            SELECT c FROM ConcertJpaEntity c
            WHERE (:keyword      IS NULL OR LOWER(c.title) LIKE :keyword
                                          OR LOWER(c.artist) LIKE :keyword
                                          OR LOWER(c.venue) LIKE :keyword)
            AND   (:category     IS NULL OR c.category    = :category)
            AND   (:city         IS NULL OR c.city        = :city)
            AND   (:organizerId  IS NULL OR c.organizerId = :organizerId)
            """)
    Page<ConcertJpaEntity> findForAdmin(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("city") String city,
            @Param("organizerId") UUID organizerId,
            Pageable pageable);
}
