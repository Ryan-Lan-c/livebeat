CREATE TABLE auth.users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    username      VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255),
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    auth_provider VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    organizer_id  UUID,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by    UUID         NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    updated_by    UUID         NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT fk_users_organizer FOREIGN KEY (organizer_id)
        REFERENCES auth.users(id) ON DELETE SET NULL
);

COMMENT ON TABLE  auth.users                IS 'User accounts | 使用者帳號';
COMMENT ON COLUMN auth.users.id             IS 'Primary key | 主鍵';
COMMENT ON COLUMN auth.users.email          IS 'Email address used for login | 登入用電子郵件';
COMMENT ON COLUMN auth.users.username       IS 'Display name | 顯示名稱';
COMMENT ON COLUMN auth.users.password_hash  IS 'Hashed password; NULL for OAuth users | 密碼雜湊值，OAuth 使用者為 NULL';
COMMENT ON COLUMN auth.users.role           IS 'USER | ORGANIZER | STAFF | ADMIN';
COMMENT ON COLUMN auth.users.auth_provider  IS 'LOCAL | GOOGLE | FACEBOOK';
COMMENT ON COLUMN auth.users.enabled        IS 'Whether the account is active | 帳號是否啟用';
COMMENT ON COLUMN auth.users.organizer_id   IS 'FK to the ORGANIZER who manages this STAFF account; NULL for USER/ORGANIZER/ADMIN | STAFF 所屬的主辦方 UUID，其他角色為 NULL';
COMMENT ON COLUMN auth.users.created_at     IS 'Creation timestamp (UTC) | 建立時間';
COMMENT ON COLUMN auth.users.updated_at     IS 'Last update timestamp (UTC) | 最後更新時間';
COMMENT ON COLUMN auth.users.created_by     IS 'UUID of the actor who created this record; 00000000-... for system operations | 建立者，系統操作為全零 UUID';
COMMENT ON COLUMN auth.users.updated_by     IS 'UUID of the actor who last updated this record | 最後更新者';

CREATE TABLE auth.refresh_tokens (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,
    token      VARCHAR(512) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES auth.users(id) ON DELETE CASCADE
);

COMMENT ON TABLE  auth.refresh_tokens            IS 'Refresh tokens; supports server-side revocation | Refresh Token，支援伺服器端撤銷（登出即失效）';
COMMENT ON COLUMN auth.refresh_tokens.id         IS 'Primary key | 主鍵';
COMMENT ON COLUMN auth.refresh_tokens.user_id    IS 'Owner of this token | 此 Token 所屬使用者';
COMMENT ON COLUMN auth.refresh_tokens.token      IS 'Opaque refresh token value (unique) | Refresh Token 值（唯一）';
COMMENT ON COLUMN auth.refresh_tokens.expires_at IS 'Expiry timestamp (UTC) | 過期時間';
COMMENT ON COLUMN auth.refresh_tokens.revoked    IS 'True if the token has been revoked | 是否已撤銷（登出後為 true）';
COMMENT ON COLUMN auth.refresh_tokens.created_at IS 'Creation timestamp (UTC) | 建立時間';

CREATE INDEX idx_users_email ON auth.users(email);
CREATE INDEX idx_refresh_tokens_user_id ON auth.refresh_tokens(user_id);

CREATE TABLE event_publication (
    id                   UUID                        NOT NULL,
    listener_id          VARCHAR(512)                NOT NULL,
    event_type           VARCHAR(512)                NOT NULL,
    serialized_event     TEXT                        NOT NULL,
    publication_date     TIMESTAMPTZ(6) NOT NULL,
    completion_date      TIMESTAMPTZ(6),
    completion_attempts  INTEGER                     NOT NULL DEFAULT 0,
    last_resubmission_date TIMESTAMPTZ(6),
    status               VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE INDEX idx_event_publication_completion_date ON event_publication (completion_date);
