package com.livebeat.concert.api;

import com.livebeat.concert.application.dto.*;
import com.livebeat.concert.application.service.ConcertService;
import com.livebeat.shared.ApiVersion;
import com.livebeat.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * [concert] 演唱會後台管理 REST API 控制器
 *
 * 負責：演唱會 CRUD、狀態流轉、場次與票區管理、封面圖上傳
 * 對應路由：/api/v1/admin/concerts/**
 * 權限：ADMIN（全部）、ORGANIZER（限自有演唱會）— 路由層由 SecurityConfig 控制
 * 依賴：ConcertService
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/admin/concerts")
@RequiredArgsConstructor
public class AdminConcertController {

    private final ConcertService concertService;

    // ─── Concert ─────────────────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConcertDetailResponse createConcert(
            @Valid @RequestBody CreateConcertRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.createConcert(request, principal.userId());
    }

    @PutMapping("/{id}")
    public ConcertDetailResponse updateConcert(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateConcertRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.updateConcert(id, request, principal.userId());
    }

    @PatchMapping("/{id}/status")
    public ConcertDetailResponse updateConcertStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.updateConcertStatus(id, request.status(), principal.userId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConcert(@PathVariable UUID id) {
        concertService.deleteConcert(id);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageUploadResponse uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        String url = concertService.uploadImage(id, file, principal.userId());
        return new ImageUploadResponse(url);
    }

    // ─── Session ─────────────────────────────────────────────

    @PostMapping("/{id}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public ConcertSessionResponse addSession(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSessionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.addSession(id, request, principal.userId());
    }

    @PutMapping("/{id}/sessions/{sid}")
    public ConcertSessionResponse updateSession(
            @PathVariable UUID id,
            @PathVariable UUID sid,
            @Valid @RequestBody UpdateSessionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.updateSession(id, sid, request, principal.userId());
    }

    @PatchMapping("/{id}/sessions/{sid}/status")
    public ConcertSessionResponse updateSessionStatus(
            @PathVariable UUID id,
            @PathVariable UUID sid,
            @Valid @RequestBody UpdateSessionStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.updateSessionStatus(id, sid, request.status(), principal.userId());
    }

    @DeleteMapping("/{id}/sessions/{sid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable UUID id, @PathVariable UUID sid) {
        concertService.deleteSession(id, sid);
    }

    // ─── Ticket Zone ─────────────────────────────────────────

    @PostMapping("/{id}/sessions/{sid}/zones")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketZoneResponse addTicketZone(
            @PathVariable UUID id,
            @PathVariable UUID sid,
            @Valid @RequestBody CreateTicketZoneRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.addTicketZone(id, sid, request, principal.userId());
    }

    @PutMapping("/{id}/sessions/{sid}/zones/{zid}")
    public TicketZoneResponse updateTicketZone(
            @PathVariable UUID id,
            @PathVariable UUID sid,
            @PathVariable UUID zid,
            @Valid @RequestBody UpdateTicketZoneRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return concertService.updateTicketZone(id, sid, zid, request, principal.userId());
    }

    @DeleteMapping("/{id}/sessions/{sid}/zones/{zid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTicketZone(
            @PathVariable UUID id,
            @PathVariable UUID sid,
            @PathVariable UUID zid) {
        concertService.deleteTicketZone(id, sid, zid);
    }

    record ImageUploadResponse(String imageUrl) {}
}
