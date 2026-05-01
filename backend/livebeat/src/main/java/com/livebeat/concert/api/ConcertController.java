package com.livebeat.concert.api;

import com.livebeat.concert.application.dto.ConcertDetailResponse;
import com.livebeat.concert.application.dto.ConcertSessionResponse;
import com.livebeat.concert.application.dto.ConcertSummaryResponse;
import com.livebeat.concert.application.service.ConcertService;
import com.livebeat.shared.ApiVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * [concert] 演唱會公開 REST API 控制器
 *
 * 負責：提供使用者端的演唱會查詢端點（列表、詳情、場次詳情）；不需登入即可存取
 * 對應路由：GET /api/v1/concerts/**
 * 依賴：ConcertService
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    /** 公開列表搜尋：回傳 PUBLISHED/ON_SALE 及 14 天內才被 ENDED/CANCELLED 的演唱會 */
    @GetMapping
    public Page<ConcertSummaryResponse> listConcerts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return concertService.listConcerts(q, category, city, pageable);
    }

    /** 公開詳情：僅回傳一般使用者可見的演唱會；DRAFT 或超過 14 天的 ENDED/CANCELLED 回傳 404 */
    @GetMapping("/{id}")
    public ConcertDetailResponse getConcertDetail(@PathVariable UUID id) {
        return concertService.getConcertDetail(id);
    }

    /** 公開場次詳情：含該場次所有票區資訊；演唱會不可見時回傳 404 */
    @GetMapping("/{id}/sessions/{sessionId}")
    public ConcertSessionResponse getSessionDetail(
            @PathVariable UUID id,
            @PathVariable UUID sessionId) {
        return concertService.getPublicSessionDetail(id, sessionId);
    }
}
