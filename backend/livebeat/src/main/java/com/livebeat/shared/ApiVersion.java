package com.livebeat.shared;

/**
 * [shared] API 版本路徑前綴常數
 *
 * 負責：統一定義各版本 API 的路徑前綴，供 Controller 與 SecurityConfig 使用，避免字串散落各處
 */
public final class ApiVersion {
    public static final String V1 = "/api/v1";

    private ApiVersion() {}
}
