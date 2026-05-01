package com.livebeat.shared.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

/**
 * [shared] Spring Security UserDetails 實作
 *
 * 負責：封裝已認證使用者的 UUID、email、角色資訊，供 SecurityContext 與 AuditorAware 使用
 */
public record UserPrincipal(
        UUID userId,
        String email,
        String passwordHash,
        Collection<? extends GrantedAuthority> authorities,
        boolean enabled
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /** 回傳第一個 ROLE_ 開頭的 authority 字串，例如 "ROLE_ORGANIZER" */
    public String role() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .orElse("");
    }
}
