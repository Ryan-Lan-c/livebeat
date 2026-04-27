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
 * 負責：驗證演唱會 CRUD、狀態流轉驗證、票區刪除保護、圖片上傳的業務邏輯
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
                "演唱會", "歌手", "描述", "場館", "台北", "TW", ConcertCategory.POP);
        Concert saved = buildConcert(UUID.randomUUID(), ConcertStatus.DRAFT);
        when(concertRepository.save(any())).thenReturn(saved);
        when(sessionRepository.findByConcertId(saved.getId())).thenReturn(List.of());

        ConcertDetailResponse response = concertService.createConcert(req, actorId);

        assertThat(response.status()).isEqualTo(ConcertStatus.DRAFT);
        assertThat(response.sessions()).isEmpty();
    }

    @Test
    void createConcert_defaults_country_to_TW_when_blank() {
        UUID actorId = UUID.randomUUID();
        CreateConcertRequest req = new CreateConcertRequest(
                "演唱會", "歌手", null, "場館", "台北", null, ConcertCategory.POP);
        Concert saved = buildConcert(UUID.randomUUID(), ConcertStatus.DRAFT);
        when(concertRepository.save(any())).thenReturn(saved);
        when(sessionRepository.findByConcertId(any())).thenReturn(List.of());

        concertService.createConcert(req, actorId);

        verify(concertRepository).save(argThat(c -> "TW".equals(c.getCountry())));
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
                concertId, ConcertStatus.PUBLISHED, UUID.randomUUID());

        assertThat(response.status()).isEqualTo(ConcertStatus.PUBLISHED);
    }

    @Test
    void updateStatus_DRAFT_to_ON_SALE_fails() {
        UUID concertId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.DRAFT)));

        assertThatThrownBy(() -> concertService.updateConcertStatus(
                concertId, ConcertStatus.ON_SALE, UUID.randomUUID()))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Test
    void updateStatus_ENDED_to_PUBLISHED_fails() {
        UUID concertId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.ENDED)));

        assertThatThrownBy(() -> concertService.updateConcertStatus(
                concertId, ConcertStatus.PUBLISHED, UUID.randomUUID()))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Test
    void updateStatus_ENDED_to_CANCELLED_fails() {
        UUID concertId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.ENDED)));

        assertThatThrownBy(() -> concertService.updateConcertStatus(
                concertId, ConcertStatus.CANCELLED, UUID.randomUUID()))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Test
    void updateStatus_CANCELLED_to_any_fails() {
        UUID concertId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.CANCELLED)));

        assertThatThrownBy(() -> concertService.updateConcertStatus(
                concertId, ConcertStatus.PUBLISHED, UUID.randomUUID()))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION);
    }

    // ── deleteConcert ─────────────────────────────────────────────────────

    @Test
    void deleteConcert_succeeds_when_DRAFT() {
        UUID concertId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.DRAFT)));

        concertService.deleteConcert(concertId);

        verify(concertRepository).deleteById(concertId);
    }

    @Test
    void deleteConcert_fails_when_not_DRAFT() {
        UUID concertId = UUID.randomUUID();
        when(concertRepository.findById(concertId))
                .thenReturn(Optional.of(buildConcert(concertId, ConcertStatus.PUBLISHED)));

        assertThatThrownBy(() -> concertService.deleteConcert(concertId))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONCERT_DELETE_NOT_ALLOWED);
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

    // ── deleteTicketZone ──────────────────────────────────────────────────

    @Test
    void deleteTicketZone_fails_when_has_sold_tickets() {
        UUID concertId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID zoneId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(buildSession(sessionId, concertId)));
        TicketZone zone = TicketZone.builder()
                .id(zoneId).sessionId(sessionId).zoneCode("A").zoneName("A區")
                .price(1000).totalSeats(100).soldSeats(1).lockedSeats(0).build();
        when(zoneRepository.findById(zoneId)).thenReturn(Optional.of(zone));

        assertThatThrownBy(() -> concertService.deleteTicketZone(concertId, sessionId, zoneId))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.ZONE_DELETE_NOT_ALLOWED);
    }

    // ── listConcerts ──────────────────────────────────────────────────────

    @Test
    void listConcerts_formats_keyword_with_wildcards() {
        var pageable = PageRequest.of(0, 20);
        when(concertRepository.searchPublic(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        concertService.listConcerts("jay", null, null, pageable);

        verify(concertRepository).searchPublic(eq("%jay%"), isNull(), isNull(), eq(pageable));
    }

    @Test
    void listConcerts_passes_null_keyword_when_query_is_blank() {
        var pageable = PageRequest.of(0, 20);
        when(concertRepository.searchPublic(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        concertService.listConcerts("  ", null, null, pageable);

        verify(concertRepository).searchPublic(isNull(), isNull(), isNull(), eq(pageable));
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

    private ConcertSession buildSession(UUID id, UUID concertId) {
        return ConcertSession.builder()
                .id(id).concertId(concertId).sessionName("Day 1")
                .eventDate(Instant.now().plusSeconds(86400))
                .status(SessionStatus.DRAFT)
                .hasAssignedSeats(false).maxTicketsPerOrder(4)
                .build();
    }
}
