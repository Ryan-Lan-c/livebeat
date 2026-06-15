package com.livebeat.concert.application.service;

import com.livebeat.shared.exception.ApiException;
import com.livebeat.shared.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * [concert] ImageType magic-bytes 偵測單元測試
 *
 * 負責：驗證僅 PNG/JPEG/WebP 由實際檔頭被接受；偽造（如謊報的 SVG）與空內容被拒。
 */
class ImageTypeTest {

    @Test
    void detects_png_by_magic_bytes() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
        assertThat(ImageType.detect(png).contentType()).isEqualTo("image/png");
    }

    @Test
    void detects_jpeg_by_magic_bytes() {
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x10};
        assertThat(ImageType.detect(jpeg).contentType()).isEqualTo("image/jpeg");
    }

    @Test
    void detects_webp_by_magic_bytes() {
        byte[] webp = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 0};
        assertThat(ImageType.detect(webp).contentType()).isEqualTo("image/webp");
    }

    @Test
    void rejects_svg_with_script_even_if_declared_as_image() {
        byte[] svg = "<svg xmlns=\"...\"><script>alert(1)</script></svg>".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> ImageType.detect(svg))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_FILE_TYPE);
    }

    @Test
    void rejects_empty_content() {
        assertThatThrownBy(() -> ImageType.detect(new byte[0]))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_FILE_TYPE);
    }
}
