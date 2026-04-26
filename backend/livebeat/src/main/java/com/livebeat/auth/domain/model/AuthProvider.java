package com.livebeat.auth.domain.model;

/**
 * [auth] 登入方式列舉
 *
 * 負責：定義使用者帳號的登入來源（本地帳號或第三方 OAuth）
 */
public enum AuthProvider {
    LOCAL, GOOGLE, FACEBOOK
}
