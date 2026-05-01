package com.livebeat.auth.domain.port;

import com.livebeat.auth.domain.model.OrganizerProfile;

import java.util.Optional;
import java.util.UUID;

/**
 * [auth] OrganizerProfile Repository Port（次要埠，由 Infrastructure 實作）
 *
 * 負責：定義主辦方業務資料的持久化操作契約
 */
public interface OrganizerProfileRepository {
    Optional<OrganizerProfile> findByUserId(UUID userId);
    OrganizerProfile save(OrganizerProfile profile);
}
