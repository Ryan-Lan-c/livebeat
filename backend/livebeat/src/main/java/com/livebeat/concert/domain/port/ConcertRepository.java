package com.livebeat.concert.domain.port;

import com.livebeat.concert.domain.model.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * [concert] 演唱會 Repository Port（次要埠，由 Infrastructure 實作）
 *
 * 負責：定義演唱會的持久化操作契約；搜尋只回傳 PUBLISHED / ON_SALE 狀態的演唱會
 */
public interface ConcertRepository {
    Concert save(Concert concert);
    Optional<Concert> findById(UUID id);
    Page<Concert> searchPublic(String keyword, String category, String city, Pageable pageable);
    void deleteById(UUID id);
    boolean existsById(UUID id);
}
