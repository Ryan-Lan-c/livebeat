package com.livebeat.concert.domain.model;

/**
 * [concert] 演唱會狀態
 *
 * 負責：定義演唱會的生命週期狀態；狀態流轉由 ConcertService 負責驗證
 * DRAFT → PUBLISHED → ON_SALE → ENDED｜CANCELLED；ENDED 與 CANCELLED 為終態，不可再轉換
 */
public enum ConcertStatus {
    DRAFT,
    PUBLISHED,
    ON_SALE,
    CANCELLED,
    ENDED
}
