package com.livebeat.concert.domain.port;

import java.io.InputStream;

/**
 * [concert] 物件儲存 Port（次要埠，由 MinioStorageAdapter 實作）
 *
 * 負責：定義檔案上傳與刪除的契約；bucket 由 adapter 的設定決定，呼叫端只需傳入 key 與資料流
 */
public interface StoragePort {
    /**
     * 上傳檔案並回傳可存取的 URL。
     */
    String store(String key, InputStream data, long contentLength, String contentType);

    void remove(String key);
}
