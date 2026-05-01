package com.livebeat.concert.api;

import com.livebeat.concert.api.dto.*;
import com.livebeat.concert.application.dto.*;
import com.livebeat.concert.application.service.ConcertService;
import com.livebeat.shared.ApiVersion;
import com.livebeat.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * [concert] 演唱會後台管理 REST API 控制器
 *
 * 負責：演唱會 CRUD、狀態流轉、場次與票區管理、封面圖上傳
 * 對應路由：/api/v1/admin/concerts/**
 * 權限：ADMIN（全部演唱會）、ORGANIZER（限自有演唱會）；DELETE concert 限 ADMIN
 * 依賴：ConcertService
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/admin/concerts")
@RequiredArgsConstructor
public class AdminConcertController {

    private final ConcertService concertService;

    // ─── Concert ─────────────────────────────────────────────

    /** 後台列表：不過濾狀態；ORGANIZER 只看自己的演唱會 */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public Page<ConcertSummaryResponse> listConcerts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.listConcertsAdmin(q, category, city,
                principal.userId(), principal.role(), pageable);
    }

    /** 後台詳情：不過濾狀態；ORGANIZER 只能看自己的演唱會 */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ConcertDetailResponse getConcertDetail(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.getConcertDetailAdmin(id, principal.userId(), principal.role());
    }

    /** 建立演唱會（DRAFT 狀態）；organizerId 選填，ADMIN 可指定主辦方 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ConcertDetailResponse createConcert(
            @Valid @RequestBody CreateConcertRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.createConcert(request, principal.userId(), principal.role());
    }

    /** 更新演唱會基本資訊（標題、藝人、場地等） */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ConcertDetailResponse updateConcert(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateConcertRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.updateConcert(id, request, principal.userId(), principal.role());
    }

    /** 更新演唱會狀態；合法流轉：DRAFT→PUBLISHED→ON_SALE→ENDED｜CANCELLED */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ConcertDetailResponse updateConcertStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.updateConcertStatus(id, request.status(), principal.userId(), principal.role());
    }

    /** 刪除演唱會；僅 DRAFT 狀態可刪除；限 ADMIN */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteConcert(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        concertService.deleteConcert(id, principal.userId(), principal.role());
    }

    /** 上傳演唱會封面圖至 MinIO；僅接受 image/* 類型 */
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ImageUploadResponse uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        String url = concertService.uploadImage(id, file, principal.userId(), principal.role());
        return new ImageUploadResponse(url);
    }

    // ─── Session ─────────────────────────────────────────────

    /** 新增場次至指定演唱會 */
    @PostMapping("/{id}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ConcertSessionResponse addSession(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSessionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.addSession(id, request, principal.userId(), principal.role());
    }

    /** 更新場次基本資訊（場次名稱、活動時間、售票時間等） */
    @PutMapping("/{id}/sessions/{sid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ConcertSessionResponse updateSession(
            @PathVariable UUID id,
            @PathVariable UUID sid,
            @Valid @RequestBody UpdateSessionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.updateSession(id, sid, request, principal.userId(), principal.role());
    }

    /** 更新場次狀態；合法流轉：DRAFT→ON_SALE→SOLD_OUT→ENDED｜CANCELLED */
    @PatchMapping("/{id}/sessions/{sid}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ConcertSessionResponse updateSessionStatus(
            @PathVariable UUID id,
            @PathVariable UUID sid,
            @Valid @RequestBody UpdateSessionStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.updateSessionStatus(id, sid, request.status(), principal.userId(), principal.role());
    }

    /** 刪除場次；僅 DRAFT 狀態可刪除 */
    @DeleteMapping("/{id}/sessions/{sid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public void deleteSession(
            @PathVariable UUID id,
            @PathVariable UUID sid,
            @AuthenticationPrincipal UserPrincipal principal) {
        concertService.deleteSession(id, sid, principal.userId(), principal.role());
    }

    // ─── Ticket Zone ─────────────────────────────────────────

    /** 新增票區至指定場次 */
    @PostMapping("/{id}/sessions/{sid}/zones")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public TicketZoneResponse addTicketZone(
            @PathVariable UUID id,
            @PathVariable UUID sid,
            @Valid @RequestBody CreateTicketZoneRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.addTicketZone(id, sid, request, principal.userId(), principal.role());
    }

    /** 更新票區資訊（名稱、票價、總座位數） */
    @PutMapping("/{id}/sessions/{sid}/zones/{zid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public TicketZoneResponse updateTicketZone(
            @PathVariable UUID id,
            @PathVariable UUID sid,
            @PathVariable UUID zid,
            @Valid @RequestBody UpdateTicketZoneRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.updateTicketZone(id, sid, zid, request, principal.userId(), principal.role());
    }

    /** 刪除票區；已有售出或鎖定票數時拒絕 */
    @DeleteMapping("/{id}/sessions/{sid}/zones/{zid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public void deleteTicketZone(
            @PathVariable UUID id,
            @PathVariable UUID sid,
            @PathVariable UUID zid,
            @AuthenticationPrincipal UserPrincipal principal) {
        concertService.deleteTicketZone(id, sid, zid, principal.userId(), principal.role());
    }

    record ImageUploadResponse(String imageUrl) {}
}
