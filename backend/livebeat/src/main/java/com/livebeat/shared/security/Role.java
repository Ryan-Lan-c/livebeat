package com.livebeat.shared.security;

/**
 * [shared] 系統角色列舉
 *
 * 負責：以型別安全的列舉取代散落各處的 "ROLE_..." 字串比對。
 *       authority() 對應 Spring Security 的 GrantedAuthority 字串（ROLE_ 前綴）。
 */
public enum Role {
    USER,
    ORGANIZER,
    STAFF,
    ADMIN;

    /** 對應 Spring Security 權限字串，例如 ADMIN -&gt; "ROLE_ADMIN"。 */
    public String authority() {
        return "ROLE_" + name();
    }

    /** 判斷給定的 authority 字串是否為本角色。 */
    public boolean matches(String authority) {
        return authority().equals(authority);
    }
}
