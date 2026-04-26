package com.livebeat.auth.domain.model;

/**
 * [auth] 使用者角色列舉
 *
 * 負責：定義系統中四種角色（USER / ORGANIZER / STAFF / ADMIN），各司其職，權限層層遞增
 */
public enum UserRole {
    USER, ORGANIZER, STAFF, ADMIN
}
