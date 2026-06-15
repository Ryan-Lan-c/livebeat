package com.livebeat.concert.application.service;

import com.livebeat.shared.exception.ApiException;
import com.livebeat.shared.exception.ErrorCode;

/**
 * [concert] 允許上傳的封面圖型別 allowlist + magic bytes 偵測
 *
 * 負責：不信任 client 提供的 Content-Type，改以檔頭實際 bytes 判定真實格式，
 *       拒絕偽造（例如把夾帶 &lt;script&gt; 的 SVG 謊報成 image/png）。
 *       回傳的 contentType 為由內容判定的權威值，供寫入物件儲存的 metadata。
 */
enum ImageType {
    PNG("image/png"),
    JPEG("image/jpeg"),
    WEBP("image/webp");

    private final String contentType;

    ImageType(String contentType) {
        this.contentType = contentType;
    }

    String contentType() {
        return contentType;
    }

    /**
     * 以 magic bytes 判定實際型別，且必須落在 allowlist 內；
     * 無法判定或非允許型別時拋 {@link ErrorCode#INVALID_FILE_TYPE}。
     */
    static ImageType detect(byte[] content) {
        if (isPng(content)) return PNG;
        if (isJpeg(content)) return JPEG;
        if (isWebp(content)) return WEBP;
        throw new ApiException(ErrorCode.INVALID_FILE_TYPE);
    }

    private static boolean isPng(byte[] b) {
        return b.length >= 8
                && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && (b[4] & 0xFF) == 0x0D && (b[5] & 0xFF) == 0x0A
                && (b[6] & 0xFF) == 0x1A && (b[7] & 0xFF) == 0x0A;
    }

    private static boolean isJpeg(byte[] b) {
        return b.length >= 3
                && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }

    private static boolean isWebp(byte[] b) {
        // RIFF....WEBP（位元組 0-3 = "RIFF"，8-11 = "WEBP"）
        return b.length >= 12
                && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }
}
