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
 *
 * 搜尋策略：pg_trgm GIN 索引加速 ILIKE，similarity() 提供相關性排序；
 * rawKeyword 為不含 % 的原始輸入，供 similarity() 計算使用。
 */
interface ConcertJpaRepository extends JpaRepository<ConcertJpaEntity, UUID> {

    @Query(
        value = """
            SELECT * FROM concert.concerts c
            WHERE (
                c.status IN ('PUBLISHED', 'ON_SALE')
                OR (c.status = 'CANCELLED' AND c.cancelled_at IS NOT NULL AND c.cancelled_at >= :cutoffTime)
                OR (c.status = 'ENDED'     AND c.ended_at     IS NOT NULL AND c.ended_at     >= :cutoffTime)
            )
            AND (:keyword  IS NULL OR c.title ILIKE :keyword OR c.artist ILIKE :keyword OR c.venue ILIKE :keyword)
            AND (:category IS NULL OR c.category = :category)
            AND (:city     IS NULL OR c.city = :city)
            ORDER BY
                CASE WHEN :rawKeyword IS NULL THEN 0
                     ELSE GREATEST(
                         similarity(c.title,  :rawKeyword),
                         similarity(c.artist, :rawKeyword),
                         similarity(c.venue,  :rawKeyword)
                     )
                END DESC,
                c.created_at DESC
            """,
        countQuery = """
            SELECT COUNT(*) FROM concert.concerts c
            WHERE (
                c.status IN ('PUBLISHED', 'ON_SALE')
                OR (c.status = 'CANCELLED' AND c.cancelled_at IS NOT NULL AND c.cancelled_at >= :cutoffTime)
                OR (c.status = 'ENDED'     AND c.ended_at     IS NOT NULL AND c.ended_at     >= :cutoffTime)
            )
            AND (:keyword  IS NULL OR c.title ILIKE :keyword OR c.artist ILIKE :keyword OR c.venue ILIKE :keyword)
            AND (:category IS NULL OR c.category = :category)
            AND (:city     IS NULL OR c.city = :city)
            """,
        nativeQuery = true
    )
    Page<ConcertJpaEntity> findPublic(
            @Param("cutoffTime") Instant cutoffTime,
            @Param("keyword") String keyword,
            @Param("rawKeyword") String rawKeyword,
            @Param("category") String category,
            @Param("city") String city,
            Pageable pageable);

    @Query(
        value = """
            SELECT * FROM concert.concerts c
            WHERE (:keyword     IS NULL OR c.title ILIKE :keyword OR c.artist ILIKE :keyword OR c.venue ILIKE :keyword)
            AND   (:category    IS NULL OR c.category     = :category)
            AND   (:city        IS NULL OR c.city         = :city)
            AND   (:organizerId IS NULL OR c.organizer_id = CAST(:organizerId AS uuid))
            ORDER BY
                CASE WHEN :rawKeyword IS NULL THEN 0
                     ELSE GREATEST(
                         similarity(c.title,  :rawKeyword),
                         similarity(c.artist, :rawKeyword),
                         similarity(c.venue,  :rawKeyword)
                     )
                END DESC,
                c.created_at DESC
            """,
        countQuery = """
            SELECT COUNT(*) FROM concert.concerts c
            WHERE (:keyword     IS NULL OR c.title ILIKE :keyword OR c.artist ILIKE :keyword OR c.venue ILIKE :keyword)
            AND   (:category    IS NULL OR c.category     = :category)
            AND   (:city        IS NULL OR c.city         = :city)
            AND   (:organizerId IS NULL OR c.organizer_id = CAST(:organizerId AS uuid))
            """,
        nativeQuery = true
    )
    Page<ConcertJpaEntity> findForAdmin(
            @Param("keyword") String keyword,
            @Param("rawKeyword") String rawKeyword,
            @Param("category") String category,
            @Param("city") String city,
            @Param("organizerId") UUID organizerId,
            Pageable pageable);
}
