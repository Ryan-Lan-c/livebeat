package com.livebeat.auth.api;

import com.livebeat.auth.api.dto.UpdateUserProfileRequest;
import com.livebeat.auth.application.dto.UserProfileResponse;
import com.livebeat.auth.application.service.ProfileService;
import com.livebeat.shared.ApiVersion;
import com.livebeat.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * [auth] 使用者個人資料 REST API 控制器
 *
 * 負責：查詢與更新當前登入使用者的擴充個人資料（avatar、bio）；資料不存在時回傳空值而非 404
 * 對應路由：GET /PUT /api/v1/auth/profile
 * 權限：需登入（任何角色）
 * 依賴：ProfileService
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/auth/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /** 取得當前使用者的個人資料；尚未設定時欄位皆為 null */
    @GetMapping
    public UserProfileResponse getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return profileService.getUserProfile(principal.userId());
    }

    /** 更新當前使用者的個人資料；null 欄位維持原值 */
    @PutMapping
    public UserProfileResponse updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return profileService.updateUserProfile(principal.userId(), request);
    }
}
