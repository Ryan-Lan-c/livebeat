package com.livebeat.concert.application.service;

import com.livebeat.concert.api.dto.*;
import com.livebeat.concert.application.dto.*;
import com.livebeat.concert.domain.model.*;
import com.livebeat.concert.domain.port.*;
import com.livebeat.shared.exception.ApiException;
import com.livebeat.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * [concert] 演唱會應用服務
 *
 * 負責：演唱會、場次、票區的 CRUD；Concert / Session 狀態流轉驗證；封面圖上傳至 MinIO；
 *       公開可見性判斷（一般使用者僅見 PUBLISHED/ON_SALE 及 14 天內的 ENDED/CANCELLED）；
 *       所有權驗證（ORGANIZER 只能操作自有演唱會；ADMIN 可操作全部）
 * 依賴：ConcertRepository, ConcertSessionRepository, TicketZoneRepository, StoragePort
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ConcertService {

    /** 公開 API：ENDED / CANCELLED 演唱會的可見視窗（天數） */
    private static final int PUBLIC_VISIBILITY_DAYS = 14;

    private final ConcertRepository concertRepository;
    private final ConcertSessionRepository sessionRepository;
    private final TicketZoneRepository zoneRepository;
    private final StoragePort storagePort;

    // ─── Concert（公開） ─────────────────────────────────────

    /** 公開列表搜尋：PUBLISHED/ON_SALE，以及 14 天內才被 ENDED/CANCELLED 的演唱會 */
    @Transactional(readOnly = true)
    public Page<ConcertSummaryResponse> listConcerts(String q, String category, String city, Pageable pageable) {
        String keyword = toKeyword(q);
        Instant cutoff = cutoffTime();
        return concertRepository.searchPublic(keyword, category, city, cutoff, pageable)
                .map(ConcertSummaryResponse::from);
    }

    /** 公開詳情：僅回傳一般使用者可見的演唱會；不可見者統一回傳 404 */
    @Transactional(readOnly = true)
    public ConcertDetailResponse getConcertDetail(UUID concertId) {
        Concert concert = findPublicConcert(concertId);
        return buildDetailResponse(concert);
    }

    @Transactional(readOnly = true)
    public ConcertSessionResponse getPublicSessionDetail(UUID concertId, UUID sessionId) {
        findPublicConcert(concertId);
        ConcertSession session = findSessionById(sessionId, concertId);
        List<TicketZoneResponse> zones = zoneRepository.findBySessionId(sessionId)
                .stream().map(TicketZoneResponse::from).toList();
        return ConcertSessionResponse.from(session, zones);
    }

    // ─── Concert（後台） ─────────────────────────────────────

    /** 後台列表搜尋：不過濾狀態；ORGANIZER 只能看自己的演唱會 */
    @Transactional(readOnly = true)
    public Page<ConcertSummaryResponse> listConcertsAdmin(String q, String category, String city,
                                                           UUID actorId, String actorRole, Pageable pageable) {
        String keyword = toKeyword(q);
        UUID organizerFilter = "ROLE_ORGANIZER".equals(actorRole) ? actorId : null;
        return concertRepository.searchAdmin(keyword, category, city, organizerFilter, pageable)
                .map(ConcertSummaryResponse::from);
    }

    /** 後台詳情：不過濾狀態；ORGANIZER 只能看自己的演唱會，否則回傳 404 */
    @Transactional(readOnly = true)
    public ConcertDetailResponse getConcertDetailAdmin(UUID concertId, UUID actorId, String actorRole) {
        Concert concert = findConcertById(concertId);
        checkOwnership(concert, actorId, actorRole);
        return buildDetailResponse(concert);
    }

    /** ADMIN 可指定 organizerId；ORGANIZER 一律使用自己的 actorId，忽略請求中的 organizerId */
    public ConcertDetailResponse createConcert(CreateConcertRequest req, UUID actorId, String actorRole) {
        String country = (req.country() != null && !req.country().isBlank()) ? req.country() : "TW";
        UUID organizerId = ("ROLE_ADMIN".equals(actorRole) && req.organizerId() != null)
                ? req.organizerId() : actorId;
        Concert concert = Concert.create(req.title(), req.artist(), req.description(),
                req.venue(), req.city(), country, req.category(), organizerId);
        Concert saved = concertRepository.save(concert);
        return buildDetailResponse(saved);
    }

    public ConcertDetailResponse updateConcert(UUID concertId, UpdateConcertRequest req, UUID actorId, String actorRole) {
        Concert concert = findConcertAndCheckOwnership(concertId, actorId, actorRole);
        Concert updated = concert
                .withTitle(req.title() != null ? req.title() : concert.getTitle())
                .withArtist(req.artist() != null ? req.artist() : concert.getArtist())
                .withDescription(req.description() != null ? req.description() : concert.getDescription())
                .withVenue(req.venue() != null ? req.venue() : concert.getVenue())
                .withCity(req.city() != null ? req.city() : concert.getCity())
                .withCountry(req.country() != null ? req.country() : concert.getCountry())
                .withCategory(req.category() != null ? req.category() : concert.getCategory());
        Concert saved = concertRepository.save(updated);
        return buildDetailResponse(saved);
    }

    public ConcertDetailResponse updateConcertStatus(UUID concertId, ConcertStatus newStatus, UUID actorId, String actorRole) {
        Concert concert = findConcertAndCheckOwnership(concertId, actorId, actorRole);
        validateConcertStatusTransition(concert.getStatus(), newStatus);
        Concert updated = concert.withStatus(newStatus)
                .withCancelledAt(newStatus == ConcertStatus.CANCELLED ? Instant.now() : concert.getCancelledAt())
                .withEndedAt(newStatus == ConcertStatus.ENDED ? Instant.now() : concert.getEndedAt());
        Concert saved = concertRepository.save(updated);
        return buildDetailResponse(saved);
    }

    public void deleteConcert(UUID concertId, UUID actorId, String actorRole) {
        if (!"ROLE_ADMIN".equals(actorRole)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
        Concert concert = findConcertById(concertId);
        if (concert.getStatus() != ConcertStatus.DRAFT) {
            throw new ApiException(ErrorCode.CONCERT_DELETE_NOT_ALLOWED);
        }
        concertRepository.deleteById(concertId);
    }

    public String uploadImage(UUID concertId, MultipartFile file, UUID actorId, String actorRole) {
        Concert concert = findConcertAndCheckOwnership(concertId, actorId, actorRole);
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException(ErrorCode.INVALID_FILE_TYPE);
        }
        String ext = contentType.substring("image/".length());
        String key = "concerts/" + concertId + "/cover." + ext;
        try {
            String url = storagePort.store(key, file.getInputStream(), file.getSize(), contentType);
            concertRepository.save(concert.withImageUrl(url));
            return url;
        } catch (IOException e) {
            throw new ApiException(ErrorCode.STORAGE_UPLOAD_FAILED);
        }
    }

    // ─── Session ─────────────────────────────────────────────

    public ConcertSessionResponse addSession(UUID concertId, CreateSessionRequest req, UUID actorId, String actorRole) {
        findConcertAndCheckOwnership(concertId, actorId, actorRole);
        ConcertSession session = ConcertSession.create(
                concertId, req.sessionName(), req.eventDate(),
                req.hasAssignedSeats(), req.maxTicketsPerOrder(),
                req.saleStartAt(), req.saleEndAt());
        ConcertSession saved = sessionRepository.save(session);
        return ConcertSessionResponse.from(saved, List.of());
    }

    public ConcertSessionResponse updateSession(UUID concertId, UUID sessionId,
                                                 UpdateSessionRequest req, UUID actorId, String actorRole) {
        findConcertAndCheckOwnership(concertId, actorId, actorRole);
        ConcertSession session = findSessionById(sessionId, concertId);
        ConcertSession updated = session
                .withSessionName(req.sessionName() != null ? req.sessionName() : session.getSessionName())
                .withEventDate(req.eventDate() != null ? req.eventDate() : session.getEventDate())
                .withMaxTicketsPerOrder(req.maxTicketsPerOrder() != null ? req.maxTicketsPerOrder() : session.getMaxTicketsPerOrder())
                .withSaleStartAt(req.saleStartAt() != null ? req.saleStartAt() : session.getSaleStartAt())
                .withSaleEndAt(req.saleEndAt() != null ? req.saleEndAt() : session.getSaleEndAt());
        ConcertSession saved = sessionRepository.save(updated);
        List<TicketZoneResponse> zones = zoneRepository.findBySessionId(sessionId).stream()
                .map(TicketZoneResponse::from).toList();
        return ConcertSessionResponse.from(saved, zones);
    }

    public ConcertSessionResponse updateSessionStatus(UUID concertId, UUID sessionId,
                                                       SessionStatus newStatus, UUID actorId, String actorRole) {
        findConcertAndCheckOwnership(concertId, actorId, actorRole);
        ConcertSession session = findSessionById(sessionId, concertId);
        validateSessionStatusTransition(session.getStatus(), newStatus);
        ConcertSession updated = session.withStatus(newStatus);
        ConcertSession saved = sessionRepository.save(updated);
        List<TicketZoneResponse> zones = zoneRepository.findBySessionId(sessionId).stream()
                .map(TicketZoneResponse::from).toList();
        return ConcertSessionResponse.from(saved, zones);
    }

    public void deleteSession(UUID concertId, UUID sessionId, UUID actorId, String actorRole) {
        findConcertAndCheckOwnership(concertId, actorId, actorRole);
        ConcertSession session = findSessionById(sessionId, concertId);
        if (session.getStatus() != SessionStatus.DRAFT) {
            throw new ApiException(ErrorCode.SESSION_DELETE_NOT_ALLOWED);
        }
        sessionRepository.deleteById(sessionId);
    }

    // ─── Ticket Zone ─────────────────────────────────────────

    public TicketZoneResponse addTicketZone(UUID concertId, UUID sessionId,
                                             CreateTicketZoneRequest req, UUID actorId, String actorRole) {
        findConcertAndCheckOwnership(concertId, actorId, actorRole);
        findSessionById(sessionId, concertId);
        TicketZone zone = TicketZone.create(sessionId, req.zoneCode(), req.zoneName(),
                req.price(), req.totalSeats());
        return TicketZoneResponse.from(zoneRepository.save(zone));
    }

    public TicketZoneResponse updateTicketZone(UUID concertId, UUID sessionId, UUID zoneId,
                                                UpdateTicketZoneRequest req, UUID actorId, String actorRole) {
        findConcertAndCheckOwnership(concertId, actorId, actorRole);
        findSessionById(sessionId, concertId);
        TicketZone zone = zoneRepository.findById(zoneId)
                .filter(z -> z.getSessionId().equals(sessionId))
                .orElseThrow(() -> new ApiException(ErrorCode.ZONE_NOT_FOUND));
        TicketZone updated = zone
                .withZoneName(req.zoneName() != null ? req.zoneName() : zone.getZoneName())
                .withPrice(req.price() != null ? req.price() : zone.getPrice())
                .withTotalSeats(req.totalSeats() != null ? req.totalSeats() : zone.getTotalSeats());
        return TicketZoneResponse.from(zoneRepository.save(updated));
    }

    public void deleteTicketZone(UUID concertId, UUID sessionId, UUID zoneId, UUID actorId, String actorRole) {
        findConcertAndCheckOwnership(concertId, actorId, actorRole);
        findSessionById(sessionId, concertId);
        TicketZone zone = zoneRepository.findById(zoneId)
                .filter(z -> z.getSessionId().equals(sessionId))
                .orElseThrow(() -> new ApiException(ErrorCode.ZONE_NOT_FOUND));
        if (zone.hasSoldOrLockedTickets()) {
            throw new ApiException(ErrorCode.ZONE_DELETE_NOT_ALLOWED);
        }
        zoneRepository.deleteById(zoneId);
    }

    // ─── Private helpers ─────────────────────────────────────

    private Concert findConcertById(UUID concertId) {
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new ApiException(ErrorCode.CONCERT_NOT_FOUND));
    }

    private Concert findConcertAndCheckOwnership(UUID concertId, UUID actorId, String actorRole) {
        Concert concert = findConcertById(concertId);
        checkOwnership(concert, actorId, actorRole);
        return concert;
    }

    /** ORGANIZER 只能操作自有演唱會；ADMIN 無限制 */
    private void checkOwnership(Concert concert, UUID actorId, String actorRole) {
        if ("ROLE_ORGANIZER".equals(actorRole) && !concert.getOrganizerId().equals(actorId)) {
            throw new ApiException(ErrorCode.CONCERT_NOT_FOUND);
        }
    }

    /**
     * 找出對一般使用者可見的演唱會。
     * PUBLISHED/ON_SALE 無條件可見；ENDED/CANCELLED 在發生後 14 天內可見；其餘回傳 404。
     */
    private Concert findPublicConcert(UUID concertId) {
        Concert concert = findConcertById(concertId);
        Instant cutoff = cutoffTime();
        boolean visible = switch (concert.getStatus()) {
            case PUBLISHED, ON_SALE -> true;
            case ENDED     -> concert.getEndedAt()     != null && concert.getEndedAt().isAfter(cutoff);
            case CANCELLED -> concert.getCancelledAt() != null && concert.getCancelledAt().isAfter(cutoff);
            case DRAFT     -> false;
        };
        if (!visible) {
            throw new ApiException(ErrorCode.CONCERT_NOT_FOUND);
        }
        return concert;
    }

    private ConcertSession findSessionById(UUID sessionId, UUID concertId) {
        return sessionRepository.findById(sessionId)
                .filter(s -> s.getConcertId().equals(concertId))
                .orElseThrow(() -> new ApiException(ErrorCode.SESSION_NOT_FOUND));
    }

    private ConcertDetailResponse buildDetailResponse(Concert concert) {
        List<ConcertSessionResponse> sessions = sessionRepository.findByConcertId(concert.getId())
                .stream()
                .map(session -> {
                    List<TicketZoneResponse> zones = zoneRepository.findBySessionId(session.getId())
                            .stream().map(TicketZoneResponse::from).toList();
                    return ConcertSessionResponse.from(session, zones);
                })
                .toList();
        return ConcertDetailResponse.from(concert, sessions);
    }

    private void validateConcertStatusTransition(ConcertStatus current, ConcertStatus next) {
        boolean valid = switch (current) {
            case DRAFT     -> next == ConcertStatus.PUBLISHED || next == ConcertStatus.CANCELLED;
            case PUBLISHED -> next == ConcertStatus.ON_SALE   || next == ConcertStatus.CANCELLED;
            case ON_SALE   -> next == ConcertStatus.ENDED     || next == ConcertStatus.CANCELLED;
            case CANCELLED, ENDED -> false;
        };
        if (!valid) {
            throw new ApiException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    private void validateSessionStatusTransition(SessionStatus current, SessionStatus next) {
        boolean valid = switch (current) {
            case DRAFT     -> next == SessionStatus.ON_SALE  || next == SessionStatus.CANCELLED;
            case ON_SALE   -> next == SessionStatus.SOLD_OUT || next == SessionStatus.ENDED || next == SessionStatus.CANCELLED;
            case SOLD_OUT  -> next == SessionStatus.ON_SALE  || next == SessionStatus.ENDED || next == SessionStatus.CANCELLED;
            case ENDED, CANCELLED -> false;
        };
        if (!valid) {
            throw new ApiException(ErrorCode.INVALID_SESSION_STATUS_TRANSITION);
        }
    }

    private Instant cutoffTime() {
        return Instant.now().minus(PUBLIC_VISIBILITY_DAYS, ChronoUnit.DAYS);
    }

    private String toKeyword(String q) {
        return (q != null && !q.isBlank()) ? "%" + q.toLowerCase() + "%" : null;
    }
}
