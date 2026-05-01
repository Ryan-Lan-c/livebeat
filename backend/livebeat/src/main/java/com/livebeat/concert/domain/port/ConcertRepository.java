package com.livebeat.concert.domain.port;

import com.livebeat.concert.domain.model.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * [concert] 演唱會 Repository Port（次要埠，由 Infrastructure 實作）
 *
 * 負責：定義演唱會的持久化操作契約
 */
public interface ConcertRepository {
    Concert save(Concert concert);
    Optional<Concert> findById(UUID id);

    /**
     * 公開搜尋：回傳 PUBLISHED、ON_SALE，以及 cutoffTime 之後才被 CANCELLED 或 ENDED 的演唱會
     * cutoffTime 由呼叫方決定（通常為 14 天前）
     */
    Page<Concert> searchPublic(String keyword, String category, String city, Instant cutoffTime, Pageable pageable);

    /**
     * 後台搜尋：不過濾狀態；organizerId 不為 null 時只回傳該主辦方的演唱會（ORGANIZER 角色使用）
     */
    Page<Concert> searchAdmin(String keyword, String category, String city, UUID organizerId, Pageable pageable);

    void deleteById(UUID id);
    boolean existsById(UUID id);
}
