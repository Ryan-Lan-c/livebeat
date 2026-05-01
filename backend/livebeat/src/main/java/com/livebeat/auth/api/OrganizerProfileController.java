package com.livebeat.auth.api;

import com.livebeat.auth.application.dto.OrganizerProfileResponse;
import com.livebeat.auth.api.dto.UpdateOrganizerProfileRequest;
import com.livebeat.auth.application.service.ProfileService;
import com.livebeat.shared.ApiVersion;
import com.livebeat.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * [auth] 主辦方業務資料 REST API 控制器
 *
 * 負責：查詢與更新當前 ORGANIZER 的業務資料（公司名稱、介紹、網站、聯絡信箱）
 * 對應路由：GET /PUT /api/v1/admin/organizer/profile
 * 權限：ADMIN、ORGANIZER（SecurityConfig 保護 /admin/**）
 * 依賴：ProfileService
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/admin/organizer/profile")
@RequiredArgsConstructor
public class OrganizerProfileController {

    private final ProfileService profileService;

    /** 取得當前使用者的主辦方業務資料；尚未設定時欄位皆為 null */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public OrganizerProfileResponse getOrganizerProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return profileService.getOrganizerProfile(principal.userId());
    }

    /** 更新當前使用者的主辦方業務資料；null 欄位維持原值 */
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public OrganizerProfileResponse updateOrganizerProfile(
            @Valid @RequestBody UpdateOrganizerProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return profileService.updateOrganizerProfile(principal.userId(), request);
    }
}
