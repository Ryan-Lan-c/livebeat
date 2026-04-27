package com.livebeat.concert.application.service;

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
import java.util.List;
import java.util.UUID;

/**
 * [concert] 演唱會應用服務
 *
 * 負責：演唱會、場次、票區的 CRUD；狀態流轉驗證；封面圖上傳至 MinIO
 * 依賴：ConcertRepository, ConcertSessionRepository, TicketZoneRepository, StoragePort
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertSessionRepository sessionRepository;
    private final TicketZoneRepository zoneRepository;
    private final StoragePort storagePort;

    // ─── Concert ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ConcertSummaryResponse> listConcerts(String q, String category, String city, Pageable pageable) {
        String keyword = (q != null && !q.isBlank()) ? "%" + q.toLowerCase() + "%" : null;
        return concertRepository.searchPublic(keyword, category, city, pageable)
                .map(ConcertSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public ConcertDetailResponse getConcertDetail(UUID concertId) {
        Concert concert = findPublicConcert(concertId);
        return buildDetailResponse(concert);
    }

    public ConcertDetailResponse createConcert(CreateConcertRequest req, UUID actorId) {
        String country = (req.country() != null && !req.country().isBlank()) ? req.country() : "TW";
        Concert concert = Concert.create(req.title(), req.artist(), req.description(),
                req.venue(), req.city(), country, req.category(), actorId);
        Concert saved = concertRepository.save(concert);
        return buildDetailResponse(saved);
    }

    public ConcertDetailResponse updateConcert(UUID concertId, UpdateConcertRequest req, UUID actorId) {
        Concert concert = findConcertById(concertId);
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

    public ConcertDetailResponse updateConcertStatus(UUID concertId, ConcertStatus newStatus, UUID actorId) {
        Concert concert = findConcertById(concertId);
        validateStatusTransition(concert.getStatus(), newStatus);
        Concert updated = concert.withStatus(newStatus);
        Concert saved = concertRepository.save(updated);
        return buildDetailResponse(saved);
    }

    public void deleteConcert(UUID concertId) {
        Concert concert = findConcertById(concertId);
        if (concert.getStatus() != ConcertStatus.DRAFT) {
            throw new ApiException(ErrorCode.CONCERT_DELETE_NOT_ALLOWED);
        }
        concertRepository.deleteById(concertId);
    }

    public String uploadImage(UUID concertId, MultipartFile file, UUID actorId) {
        Concert concert = findConcertById(concertId);
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

    public ConcertSessionResponse addSession(UUID concertId, CreateSessionRequest req, UUID actorId) {
        findConcertById(concertId);
        ConcertSession session = ConcertSession.create(
                concertId, req.sessionName(), req.eventDate(),
                req.hasAssignedSeats(), req.maxTicketsPerOrder(),
                req.saleStartAt(), req.saleEndAt());
        ConcertSession saved = sessionRepository.save(session);
        List<TicketZoneResponse> zones = List.of();
        return ConcertSessionResponse.from(saved, zones);
    }

    public ConcertSessionResponse updateSession(UUID concertId, UUID sessionId,
                                                 UpdateSessionRequest req, UUID actorId) {
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
                                                       SessionStatus newStatus, UUID actorId) {
        ConcertSession session = findSessionById(sessionId, concertId);
        ConcertSession updated = session.withStatus(newStatus);
        ConcertSession saved = sessionRepository.save(updated);
        List<TicketZoneResponse> zones = zoneRepository.findBySessionId(sessionId).stream()
                .map(TicketZoneResponse::from).toList();
        return ConcertSessionResponse.from(saved, zones);
    }

    public void deleteSession(UUID concertId, UUID sessionId) {
        ConcertSession session = findSessionById(sessionId, concertId);
        if (session.getStatus() != SessionStatus.DRAFT) {
            throw new ApiException(ErrorCode.SESSION_DELETE_NOT_ALLOWED);
        }
        sessionRepository.deleteById(sessionId);
    }

    // ─── Ticket Zone ─────────────────────────────────────────

    public TicketZoneResponse addTicketZone(UUID concertId, UUID sessionId,
                                             CreateTicketZoneRequest req, UUID actorId) {
        findSessionById(sessionId, concertId);
        TicketZone zone = TicketZone.create(sessionId, req.zoneCode(), req.zoneName(),
                req.price(), req.totalSeats());
        return TicketZoneResponse.from(zoneRepository.save(zone));
    }

    public TicketZoneResponse updateTicketZone(UUID concertId, UUID sessionId, UUID zoneId,
                                                UpdateTicketZoneRequest req, UUID actorId) {
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

    public void deleteTicketZone(UUID concertId, UUID sessionId, UUID zoneId) {
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

    private Concert findPublicConcert(UUID concertId) {
        Concert concert = findConcertById(concertId);
        if (concert.getStatus() == ConcertStatus.DRAFT || concert.getStatus() == ConcertStatus.CANCELLED) {
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

    private void validateStatusTransition(ConcertStatus current, ConcertStatus next) {
        boolean valid = switch (current) {
            case DRAFT -> next == ConcertStatus.PUBLISHED || next == ConcertStatus.CANCELLED;
            case PUBLISHED -> next == ConcertStatus.ON_SALE || next == ConcertStatus.CANCELLED;
            case ON_SALE -> next == ConcertStatus.ENDED || next == ConcertStatus.CANCELLED;
            case CANCELLED, ENDED -> false;
        };
        if (!valid) {
            throw new ApiException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }
}
