package com.livebeat.auth.application.service;

import com.livebeat.auth.api.dto.*;
import com.livebeat.auth.application.dto.*;
import com.livebeat.auth.domain.model.OrganizerProfile;
import com.livebeat.auth.domain.model.UserProfile;
import com.livebeat.auth.domain.port.OrganizerProfileRepository;
import com.livebeat.auth.domain.port.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * [auth] 個人資料應用服務
 *
 * 負責：使用者擴充個人資料與主辦方業務資料的查詢與更新；
 *       資料不存在時以預設空值回傳（非 404），PUT 時自動 upsert
 * 依賴：UserProfileRepository, OrganizerProfileRepository
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final OrganizerProfileRepository organizerProfileRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().userId(userId).build());
        return UserProfileResponse.from(profile);
    }

    public UserProfileResponse updateUserProfile(UUID userId, UpdateUserProfileRequest req) {
        UserProfile existing = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().userId(userId).build());
        UserProfile updated = existing
                .withAvatarUrl(req.avatarUrl() != null ? req.avatarUrl() : existing.getAvatarUrl())
                .withBio(req.bio() != null ? req.bio() : existing.getBio())
                .withPhone(req.phone() != null ? req.phone() : existing.getPhone())
                .withBirthDate(req.birthDate() != null ? req.birthDate() : existing.getBirthDate())
                .withAddress(req.address() != null ? req.address() : existing.getAddress());
        return UserProfileResponse.from(userProfileRepository.save(updated));
    }

    @Transactional(readOnly = true)
    public OrganizerProfileResponse getOrganizerProfile(UUID userId) {
        OrganizerProfile profile = organizerProfileRepository.findByUserId(userId)
                .orElse(OrganizerProfile.builder().userId(userId).build());
        return OrganizerProfileResponse.from(profile);
    }

    public OrganizerProfileResponse updateOrganizerProfile(UUID userId, UpdateOrganizerProfileRequest req) {
        OrganizerProfile existing = organizerProfileRepository.findByUserId(userId)
                .orElse(OrganizerProfile.builder().userId(userId).build());
        OrganizerProfile updated = existing
                .withCompanyName(req.companyName() != null ? req.companyName() : existing.getCompanyName())
                .withCompanyTaxId(req.companyTaxId() != null ? req.companyTaxId() : existing.getCompanyTaxId())
                .withContactPerson(req.contactPerson() != null ? req.contactPerson() : existing.getContactPerson())
                .withContactPhone(req.contactPhone() != null ? req.contactPhone() : existing.getContactPhone())
                .withDescription(req.description() != null ? req.description() : existing.getDescription())
                .withWebsite(req.website() != null ? req.website() : existing.getWebsite())
                .withContactEmail(req.contactEmail() != null ? req.contactEmail() : existing.getContactEmail());
        return OrganizerProfileResponse.from(organizerProfileRepository.save(updated));
    }
}
