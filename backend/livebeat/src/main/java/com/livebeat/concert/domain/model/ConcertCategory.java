package com.livebeat.concert.domain.model;

/**
 * [concert] 演唱會音樂類型
 *
 * 負責：定義可篩選的演唱會類別；DB 存英文代碼，前端顯示名稱由 UI 層負責轉換
 */
public enum ConcertCategory {
    POP,
    ROCK,
    HIP_HOP,
    ELECTRONIC,
    CLASSICAL,
    JAZZ,
    OTHER
}
