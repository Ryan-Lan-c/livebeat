package com.livebeat.concert.api;

import com.livebeat.concert.application.dto.ConcertDetailResponse;
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
 * 負責：提供使用者端的演唱會查詢端點（列表、詳情）；不需登入即可存取
 * 對應路由：GET /api/v1/concerts, /api/v1/concerts/{id}
 * 依賴：ConcertService
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @GetMapping
    public Page<ConcertSummaryResponse> listConcerts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return concertService.listConcerts(q, category, city, pageable);
    }

    @GetMapping("/{id}")
    public ConcertDetailResponse getConcertDetail(@PathVariable UUID id) {
        return concertService.getConcertDetail(id);
    }
}
