package com.livebeat.concert.application.service;

import com.livebeat.concert.application.dto.*;
import com.livebeat.concert.domain.model.*;
import com.livebeat.concert.domain.port.*;
import com.livebeat.shared.exception.ApiException;
import com.livebeat.shared.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * [concert] ConcertService 單元測試
 *
 * 負責：驗證演唱會 CRUD、Concert/Session 狀態流轉、公開可見性邏輯（14 天視窗）、
 *       ORGANIZER 所有權保護、票區刪除保護
 */
@ExtendWith(MockitoExtension.class)
class ConcertServiceTest {

    @Mock ConcertRepository concertRepository;
    @Mock ConcertSessionRepository sessionRepository;
    @Mock TicketZoneRepository zoneRepository;
    @Mock StoragePort storagePort;

    @InjectMocks ConcertService concertService;

    // ── createConcert ──────────────────────────────────────────────────────

    @Test
    void createConcert_saves_and_returns_detail() {
        UUID actorId = UUID.randomUUID();
        CreateConcertRequest req = new CreateConcertRequest(
                "演唱會", "歌手", "描述", "場館", "台北", "TW", ConcertCategory.POP, null);
        Concert saved = buildConcert(UUID.randomUUID(), ConcertStatus.DRAFT);
        when(concertRepository.save(any())).thenReturn(saved);
        when(sessionRepository.findByConcertId(saved.getId())).thenReturn(List.of());

        ConcertDetailResponse response = concertService.createConcert(req, actorId, "ROLE_ORGANIZER");

        assertThat(response.status()).isEqualTo(ConcertStatus.DRAFT);
        assertThat(response.sessions()).isEmpty();
    }

    @Test
    void createConcert_defaults_country_to_TW_when_blank() {
        UUID actorId = UUID.randomUUID();
        CreateConcertRequest req = new CreateConcertRequest(
                "演唱會", "歌手", null, "場館", "台北", null, ConcertCategory.POP, null);
        Concert saved = buildConcert(UUID.randomUUID(), ConcertStatus.DRAFT);
        when(concertRepository.save(any())).thenReturn(saved);
        when(sessionRepository.findByConcertId(any())).thenReturn(List.of());

        concertService.createConcert(req, actorId, "ROLE_ORGANIZER");

        verify(concertRepository).save(argThat(c -> "TW".equals(c.getCountry())));
    }

    @Test
    void createConcert_ADMIN_uses_provided_organizerId() {
        UUID actorId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        CreateConcertRequest req = new CreateConcertRequest(
                "演唱會", "歌手", null, "場館", "台北", "TW", ConcertCategory.POP, organizerId);
        Concert saved = buildConcert(UUID.randomUUID(), ConcertStatus.DRAFT);
        when(concertRepository.save(any())).thenReturn(saved);
        when(sessionRepository.findByConcertId(any())).thenReturn(List.of());

        concertService.createConcert(req, actorId, "ROLE_ADMIN");

        verify(concertRepository).save(argThat(c -> organizerId.equals(c.getOrganizerId())));
    }

    @Test
    void createConcert_ORGANIZER_always_uses_own_actorId_even_when_organizerId_specified() {
        UUID actorId = UUID.randomUUID();
        UUID anotherOrganizerId = UUID.randomUUID();
        CreateConcertRequest req = new CreateConcertRequest(
                "演唱會", "歌手", null, "場館", "台北", "TW", ConcertCategory.POP, anotherOrganizerId);
        Concert saved = buildConcert(UUID.randomUUID(), ConcertStatus.DRAFT);
        when(concertRepository.save(any())).thenReturn(saved);
        when(sessionRepository.findByConcertId(any())).thenReturn(List.of());

        concertService.createConcert(req, actorId, "ROLE_ORGANIZER");

        verify(concertRepository).save(argThat(c -> actorId.equals(c.getOrganizerId())));
    }

    @Test
    void createConcert_uses_actorId_as_organizerId_when_not_specified() {
        UUID actorId = UUID.randomUUID();
        CreateConcertRequest req = new CreateConcertRequest(
                "演唱會", "歌手", null, "場館", "台北", "TW", ConcertCategory.POP, null);
        Concert saved = buildConcert(UUID.randomUUID(), ConcertStatus.DRAFT);
        when(concertRepository.save(any())).thenReturn(saved);
        when(sessionRepository.findByConcertId(any())).thenReturn(List.of());

        concertService.createConcert(req, actorId, "ROLE_ORGANIZER");

        verify(concertRepository).save(argThat(c -> actorId.equals(c.getOrganizerId())));
    }

    // ── ownership ─────────────────────────────────────────────────────────

    @Test
    void updateConcert_ORGANIZER_fails_when_not_owner() {
        UUID concertId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Concert concert = buildConcertWithOrganizer(concertId, UUID.randomUUID()); // different owner
        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));

        assertThatThrownBy(() -> concertService.updateConcert(
                concertId,
                new UpdateConcertRequest(null, null, null, null, null, null, null),
                actorId, "ROLE_ORGANIZER"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONCERT_NOT_FOUND);
    }

    @Test
    void updateConcert_ADMIN_succeeds_regardless_of_owner() {
        UUID concertId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Concert concert = buildConcertWithOrganizer(concertId, UUID.randomUUID()); // different owner
        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));
        when(concertRepository.save(any())).thenReturn(concert);
        when(sessionRepository.findByConcertId(concertId)).thenReturn(List.of());

        concertService.updateConcert(
                concertId,
                new UpdateConcertRequest(null, null, null, null, null, null, null),
                actorId, "ROLE_ADMIN");

        verify(concertRepository).save(any());
    }

    // ── updateConcertStatus ───────────────────────────────────────────────

    @Test
    void updateStatus_DRAFT_to_PUBLISHED_succeeds() {
        UUID concertId = UUID.randomUUID();
        Concert draft = buildConcert(concertId, ConcertStatus.DRAFT);
        Concert published = draft.withStatus(ConcertStatus.PUBLISHED);
        when(concertRepository.findById(concertId)).thenReturn(Optional.of(draft));
        when(concertRepository.save(any())).thenReturn(published);
        when(sessionRepository.findByConcertId(concertId)).thenReturn(List.of());

        ConcertDetailResponse response = concertService.updateConcertStatus(
                concertId, ConcertStatus.PUBLISHED, UUID.randomUUID(), "ROLE_ADMIN");

        assertThat(response.status()).isEqualTo(ConcertStatus.PUBLISHED);
    }

    @Test
    void updateStatus_DRAFT_to_ON_SALE_fails() {
        UUID concertId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.DRAFT)));

        assertThatThrownBy(() -> concertService.updateConcertStatus(
                concertId, ConcertStatus.ON_SALE, UUID.randomUUID(), "ROLE_ADMIN"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Test
    void updateStatus_ENDED_to_PUBLISHED_fails() {
        UUID concertId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.ENDED)));

        assertThatThrownBy(() -> concertService.updateConcertStatus(
                concertId, ConcertStatus.PUBLISHED, UUID.randomUUID(), "ROLE_ADMIN"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Test
    void updateStatus_ENDED_to_CANCELLED_fails() {
        UUID concertId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.ENDED)));

        assertThatThrownBy(() -> concertService.updateConcertStatus(
                concertId, ConcertStatus.CANCELLED, UUID.randomUUID(), "ROLE_ADMIN"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Test
    void updateStatus_CANCELLED_to_any_fails() {
        UUID concertId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.CANCELLED)));

        assertThatThrownBy(() -> concertService.updateConcertStatus(
                concertId, ConcertStatus.PUBLISHED, UUID.randomUUID(), "ROLE_ADMIN"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    // ── deleteConcert ─────────────────────────────────────────────────────

    @Test
    void deleteConcert_succeeds_when_DRAFT() {
        UUID concertId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.DRAFT)));

        concertService.deleteConcert(concertId, UUID.randomUUID(), "ROLE_ADMIN");

        verify(concertRepository).deleteById(concertId);
    }

    @Test
    void deleteConcert_fails_when_not_DRAFT() {
        UUID concertId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.PUBLISHED)));

        assertThatThrownBy(() -> concertService.deleteConcert(concertId, UUID.randomUUID(), "ROLE_ADMIN"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONCERT_DELETE_NOT_ALLOWED);
    }

    @Test
    void deleteConcert_fails_when_not_ADMIN() {
        assertThatThrownBy(() -> concertService.deleteConcert(UUID.randomUUID(), UUID.randomUUID(), "ROLE_ORGANIZER"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    // ── getConcertDetail (public visibility) ──────────────────────────────

    @Test
    void getConcertDetail_throws_when_DRAFT() {
        UUID concertId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.DRAFT)));

        assertThatThrownBy(() -> concertService.getConcertDetail(concertId))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONCERT_NOT_FOUND);
    }

    @Test
    void getConcertDetail_succeeds_when_PUBLISHED() {
        UUID concertId = UUID.randomUUID();
        Concert concert = buildConcert(concertId, ConcertStatus.PUBLISHED);
        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));
        when(sessionRepository.findByConcertId(concertId)).thenReturn(List.of());

        ConcertDetailResponse response = concertService.getConcertDetail(concertId);

        assertThat(response.id()).isEqualTo(concertId);
    }

    @Test
    void getConcertDetail_succeeds_when_ENDED_within_14_days() {
        UUID concertId = UUID.randomUUID();
        Concert concert = buildConcertWithEndedAt(concertId, Instant.now().minus(7, ChronoUnit.DAYS));
        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));
        when(sessionRepository.findByConcertId(concertId)).thenReturn(List.of());

        ConcertDetailResponse response = concertService.getConcertDetail(concertId);

        assertThat(response.id()).isEqualTo(concertId);
    }

    @Test
    void getConcertDetail_throws_when_ENDED_after_14_days() {
        UUID concertId = UUID.randomUUID();
        Concert concert = buildConcertWithEndedAt(concertId, Instant.now().minus(15, ChronoUnit.DAYS));
        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));

        assertThatThrownBy(() -> concertService.getConcertDetail(concertId))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONCERT_NOT_FOUND);
    }

    @Test
    void getConcertDetail_succeeds_when_CANCELLED_within_14_days() {
        UUID concertId = UUID.randomUUID();
        Concert concert = buildConcertWithCancelledAt(concertId, Instant.now().minus(7, ChronoUnit.DAYS));
        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));
        when(sessionRepository.findByConcertId(concertId)).thenReturn(List.of());

        ConcertDetailResponse response = concertService.getConcertDetail(concertId);

        assertThat(response.id()).isEqualTo(concertId);
    }

    @Test
    void getConcertDetail_throws_when_CANCELLED_after_14_days() {
        UUID concertId = UUID.randomUUID();
        Concert concert = buildConcertWithCancelledAt(concertId, Instant.now().minus(15, ChronoUnit.DAYS));
        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));

        assertThatThrownBy(() -> concertService.getConcertDetail(concertId))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONCERT_NOT_FOUND);
    }

    // ── updateSessionStatus ───────────────────────────────────────────────

    @Test
    void updateSessionStatus_DRAFT_to_ON_SALE_succeeds() {
        UUID concertId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.ON_SALE)));
        ConcertSession draft = buildSession(sessionId, concertId, SessionStatus.DRAFT);
        ConcertSession onSale = draft.withStatus(SessionStatus.ON_SALE);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(draft));
        when(sessionRepository.save(any())).thenReturn(onSale);
        when(zoneRepository.findBySessionId(sessionId)).thenReturn(List.of());

        ConcertSessionResponse response = concertService.updateSessionStatus(
                concertId, sessionId, SessionStatus.ON_SALE, UUID.randomUUID(), "ROLE_ADMIN");

        assertThat(response.status()).isEqualTo(SessionStatus.ON_SALE);
    }

    @Test
    void updateSessionStatus_ENDED_to_ON_SALE_fails() {
        UUID concertId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.ON_SALE)));
        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(buildSession(sessionId, concertId, SessionStatus.ENDED)));

        assertThatThrownBy(() -> concertService.updateSessionStatus(
                concertId, sessionId, SessionStatus.ON_SALE, UUID.randomUUID(), "ROLE_ADMIN"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_SESSION_STATUS_TRANSITION);
    }

    @Test
    void updateSessionStatus_CANCELLED_to_any_fails() {
        UUID concertId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.ON_SALE)));
        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(buildSession(sessionId, concertId, SessionStatus.CANCELLED)));

        assertThatThrownBy(() -> concertService.updateSessionStatus(
                concertId, sessionId, SessionStatus.ON_SALE, UUID.randomUUID(), "ROLE_ADMIN"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_SESSION_STATUS_TRANSITION);
    }

    // ── deleteTicketZone ──────────────────────────────────────────────────

    @Test
    void deleteTicketZone_fails_when_has_sold_tickets() {
        UUID concertId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID zoneId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.ON_SALE)));
        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(buildSession(sessionId, concertId, SessionStatus.DRAFT)));
        TicketZone zone = TicketZone.builder()
                .id(zoneId).sessionId(sessionId).zoneCode("A").zoneName("A區")
                .price(1000).totalSeats(100).soldSeats(1).lockedSeats(0).build();
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));

        assertThatThrownBy(() -> concertService.deleteTicketZone(concertId, sessionId, zoneId, UUID.randomUUID(), "ROLE_ADMIN"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ZONE_DELETE_NOT_ALLOWED);
    }

    // ── listConcerts ──────────────────────────────────────────────────────

    @Test
    void listConcerts_formats_keyword_with_wildcards() {
        var pageable = PageRequest.of(0, 20);
        when(concertRepository.searchPublic(any(), any(), any(), any(Instant.class), any()))
                .thenReturn(new PageImpl<>(List.of()));

        concertService.listConcerts("jay", null, null, pageable);

        verify(concertRepository).searchPublic(eq("%jay%"), isNull(), isNull(), any(Instant.class), eq(pageable));
    }

    @Test
    void listConcerts_passes_null_keyword_when_query_is_blank() {
        var pageable = PageRequest.of(0, 20);
        when(concertRepository.searchPublic(any(), any(), any(), any(Instant.class), any()))
                .thenReturn(new PageImpl<>(List.of()));

        concertService.listConcerts("  ", null, null, pageable);

        verify(concertRepository).searchPublic(isNull(), isNull(), isNull(), any(Instant.class), eq(pageable));
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private Concert buildConcert(UUID id, ConcertStatus status) {
        return Concert.builder()
                .id(id).title("Test Concert").artist("Test Artist")
                .venue("Test Venue").city("台北").country("TW")
                .category(ConcertCategory.POP).status(status)
                .organizerId(UUID.randomUUID())
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }

    private Concert buildConcertWithOrganizer(UUID id, UUID organizerId) {
        return Concert.builder()
                .id(id).title("Test Concert").artist("Test Artist")
                .venue("Test Venue").city("台北").country("TW")
                .category(ConcertCategory.POP).status(ConcertStatus.DRAFT)
                .organizerId(organizerId)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }

    private Concert buildConcertWithEndedAt(UUID id, Instant endedAt) {
        return Concert.builder()
                .id(id).title("Test Concert").artist("Test Artist")
                .venue("Test Venue").city("台北").country("TW")
                .category(ConcertCategory.POP).status(ConcertStatus.ENDED)
                .organizerId(UUID.randomUUID())
                .endedAt(endedAt)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }

    private Concert buildConcertWithCancelledAt(UUID id, Instant cancelledAt) {
        return Concert.builder()
                .id(id).title("Test Concert").artist("Test Artist")
                .venue("Test Venue").city("台北").country("TW")
                .category(ConcertCategory.POP).status(ConcertStatus.CANCELLED)
                .organizerId(UUID.randomUUID())
                .cancelledAt(cancelledAt)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }

    private ConcertSession buildSession(UUID id, UUID concertId, SessionStatus status) {
        return ConcertSession.builder()
                .id(id).concertId(concertId).sessionName("Day 1")
                .eventDate(Instant.now().plusSeconds(86400))
                .status(status)
                .hasAssignedSeats(false).maxTicketsPerOrder(4)
                .build();
    }
}
