CREATE TABLE auth.users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE auth.refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES auth.users(id) ON DELETE CASCADE
);

CREATE INDEX idx_users_email ON auth.users(email);
CREATE INDEX idx_refresh_tokens_user_id ON auth.refresh_tokens(user_id);

CREATE TABLE event_publication (
    id                   UUID                        NOT NULL,
    listener_id          VARCHAR(512)                NOT NULL,
    event_type           VARCHAR(512)                NOT NULL,
    serialized_event     TEXT                        NOT NULL,
    publication_date     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    completion_date      TIMESTAMP(6) WITH TIME ZONE,
    completion_attempts  INTEGER                     NOT NULL DEFAULT 0,
    last_resubmission_date TIMESTAMP(6) WITH TIME ZONE,
    status               VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE INDEX idx_event_publication_completion_date ON event_publication (completion_date);
