package com.livebeat.concert.domain.model;

/**
 * [concert] 演唱會狀態
 *
 * 負責：定義演唱會的生命週期狀態；狀態流轉由 ConcertService 負責驗證
 * DRAFT → PUBLISHED → ON_SALE → ENDED；任一狀態可轉為 CANCELLED
 */
public enum ConcertStatus {
    DRAFT,
    PUBLISHED,
    ON_SALE,
    CANCELLED,
    ENDED
}
