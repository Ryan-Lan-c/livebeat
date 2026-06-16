package com.livebeat.concert;

import java.util.UUID;

/**
 * [concert] 跨模組售出命令 API（公開命名介面）
 *
 * 負責：供 order 模組在付款成功後確認售出，將票區 sold_seats 增量（PG 最終帳本）。
 *       與 ConcertQueryApi 的查詢分離；實作於 application 層（package-private）。
 */
public interface ConcertSaleApi {

    /** 確認售出：票區 sold_seats += quantity（@Version 樂觀鎖防併發 lost-update）。 */
    void confirmSale(UUID zoneId, int quantity);
}
