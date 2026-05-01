package com.livebeat.concert.domain.model;

/**
 * [concert] 演唱會場次狀態
 *
 * 負責：定義場次的售票生命週期
 * DRAFT → ON_SALE → SOLD_OUT → ENDED｜CANCELLED；DRAFT/ON_SALE/SOLD_OUT 可轉為 CANCELLED；ENDED 與 CANCELLED 為終態
 */
public enum SessionStatus {
    DRAFT,
    ON_SALE,
    SOLD_OUT,
    CANCELLED,
    ENDED
}
